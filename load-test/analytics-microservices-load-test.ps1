# Fixed Analytics Microservices Load Test Script
# This script tests the microservices architecture performance for the analytics service API
# Usage: .\analytics-microservices-load-test-fixed.ps1 -ConcurrentUsers 10 -TestDurationMinutes 5

param(
    [int]$ConcurrentUsers = 5,
    [int]$TestDurationMinutes = 2,
    [string]$UserServiceUrl = "http://localhost:8081",
    [string]$AnalyticsServiceUrl = "http://localhost:8084",
    [string]$ApiGatewayUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [int]$WarmupRequests = 3,
    [switch]$UseApiGateway = $false,
    [switch]$TestAllEndpoints = $false
)

# Colors for output
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"
$Magenta = "Magenta"

Write-Host "=== Analytics Microservices Load Test (Fixed) ===" -ForegroundColor $Cyan
Write-Host "User Service URL: $UserServiceUrl" -ForegroundColor $Green
Write-Host "Analytics Service URL: $AnalyticsServiceUrl" -ForegroundColor $Green
if ($UseApiGateway) {
    Write-Host "API Gateway URL: $ApiGatewayUrl (ENABLED)" -ForegroundColor $Yellow
} else {
    Write-Host "API Gateway: DISABLED (Direct service access)" -ForegroundColor $Green
}
Write-Host "Concurrent Users: $ConcurrentUsers" -ForegroundColor $Green
Write-Host "Test Duration: $TestDurationMinutes minutes" -ForegroundColor $Green
Write-Host "Warmup Requests: $WarmupRequests" -ForegroundColor $Green
Write-Host "Test All Endpoints: $TestAllEndpoints" -ForegroundColor $Green
Write-Host ""

# Global variables for metrics
$Global:Results = @()
$Global:ErrorCount = 0
$Global:SuccessCount = 0
$Global:ResponseTimes = @()

# Analytics endpoints to test
$AnalyticsEndpoints = @(
    @{
        Name = "Dashboard"
        Path = "/api/analytics/dashboard"
        Description = "Main dashboard with aggregated analytics"
        Weight = 5
    },
    @{
        Name = "Summary"
        Path = "/api/analytics/summary"
        Description = "Quick summary statistics"
        Weight = 3
    },
    @{
        Name = "Users"
        Path = "/api/analytics/users"
        Description = "User analytics and statistics"
        Weight = 2
    },
    @{
        Name = "Books"
        Path = "/api/analytics/books"
        Description = "Book analytics and inventory stats"
        Weight = 2
    },
    @{
        Name = "Transactions"
        Path = "/api/analytics/transactions"
        Description = "Transaction analytics and patterns"
        Weight = 2
    },
    @{
        Name = "Health"
        Path = "/api/analytics/health"
        Description = "Service health check"
        Weight = 1
    }
)

# Function to authenticate and get JWT token
function Get-AuthToken {
    param([string]$UserServiceUrl, [string]$Username, [string]$Password)
    
    try {
        $loginPayload = @{
            usernameOrEmail = $Username
            password = $Password
        } | ConvertTo-Json
        
        $loginUrl = if ($UseApiGateway) { "$ApiGatewayUrl/api/auth/login" } else { "$UserServiceUrl/api/auth/login" }
        
        Write-Host "Authenticating at: $loginUrl" -ForegroundColor $Yellow
        $response = Invoke-RestMethod -Uri $loginUrl -Method POST -ContentType "application/json" -Body $loginPayload -TimeoutSec 30
        
        if ($response.token) {
            Write-Host "[+] Authentication successful" -ForegroundColor $Green
            return $response.token
        } else {
            Write-Host "[-] Authentication failed - no token received" -ForegroundColor $Red
            return $null
        }
    } catch {
        Write-Host "[-] Authentication failed: $($_.Exception.Message)" -ForegroundColor $Red
        return $null
    }
}

# Function to test analytics endpoint (simplified)
function Test-AnalyticsEndpoint {
    param(
        [string]$BaseUrl, 
        [string]$Token, 
        [string]$EndpointPath, 
        [string]$EndpointName,
        [string]$RequestId
    )
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Accept" = "application/json"
    }
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $url = if ($UseApiGateway) { "$ApiGatewayUrl$EndpointPath" } else { "$BaseUrl$EndpointPath" }
        $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers -TimeoutSec 60
        $stopwatch.Stop()
        
        $responseTime = $stopwatch.ElapsedMilliseconds
        
        # Basic validation - check if response exists
        $isValid = $response -ne $null
        $dataSize = if ($response) { ($response | ConvertTo-Json).Length } else { 0 }
        
        if ($isValid) {
            $Global:SuccessCount++
            $Global:ResponseTimes += $responseTime
        } else {
            $Global:ErrorCount++
        }
        
        return [PSCustomObject]@{
            EndpointName = $EndpointName
            EndpointPath = $EndpointPath
            ResponseTime = $responseTime
            Status = if ($isValid) { "Success" } else { "Error" }
            Error = if (-not $isValid) { "Invalid response" } else { $null }
            DataSize = $dataSize
            RequestId = $RequestId
        }
        
    } catch {
        $stopwatch.Stop()
        $Global:ErrorCount++
        
        return [PSCustomObject]@{
            EndpointName = $EndpointName
            EndpointPath = $EndpointPath
            ResponseTime = $stopwatch.ElapsedMilliseconds
            Status = "Error"
            Error = $_.Exception.Message
            DataSize = 0
            RequestId = $RequestId
        }
    }
}

# Function to select endpoint based on weight (fixed)
function Get-WeightedEndpoint {
    param($Endpoints)
    
    if ($TestAllEndpoints -or $Endpoints.Count -eq 0) {
        # Return random endpoint when testing all or if no endpoints
        return $Endpoints[(Get-Random -Maximum $Endpoints.Count)]
    } else {
        # Use weighted selection - focus on dashboard and summary
        try {
            $totalWeight = ($Endpoints | ForEach-Object { $_.Weight } | Measure-Object -Sum).Sum
            if ($totalWeight -eq 0) {
                return $Endpoints[0] # Fallback to first endpoint
            }
            
            $randomValue = Get-Random -Maximum $totalWeight
            
            $currentWeight = 0
            foreach ($endpoint in $Endpoints) {
                $currentWeight += $endpoint.Weight
                if ($randomValue -lt $currentWeight) {
                    return $endpoint
                }
            }
            
            # Fallback to first endpoint
            return $Endpoints[0]
        } catch {
            Write-Host "Warning: Weight calculation failed, using random selection" -ForegroundColor $Yellow
            return $Endpoints[(Get-Random -Maximum $Endpoints.Count)]
        }
    }
}

# Function to check service health
function Test-ServiceHealth {
    Write-Host "Checking service health..." -ForegroundColor $Yellow
    
    $services = @(
        @{ Name = "User Service"; Url = "$UserServiceUrl/actuator/health" },
        @{ Name = "Analytics Service"; Url = "$AnalyticsServiceUrl/actuator/health" }
    )
    
    $allHealthy = $true
    foreach ($service in $services) {
        try {
            $response = Invoke-RestMethod -Uri $service.Url -Method GET -TimeoutSec 10
            if ($response.status -eq "UP") {
                Write-Host "  [+] $($service.Name): Healthy" -ForegroundColor $Green
            } else {
                Write-Host "  [-] $($service.Name): Unhealthy" -ForegroundColor $Red
                $allHealthy = $false
            }
        } catch {
            Write-Host "  [-] $($service.Name): Unavailable - $($_.Exception.Message)" -ForegroundColor $Red
            $allHealthy = $false
        }
    }
    
    return $allHealthy
}

# Function to generate performance report
function Generate-PerformanceReport {
    $totalRequests = $Global:SuccessCount + $Global:ErrorCount
    $successRate = if ($totalRequests -gt 0) { [math]::Round(($Global:SuccessCount / $totalRequests) * 100, 2) } else { 0 }
    $avgResponseTime = if ($Global:ResponseTimes.Count -gt 0) { [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2) } else { 0 }
    $throughput = if ($TestDurationMinutes -gt 0) { [math]::Round($totalRequests / ($TestDurationMinutes * 60), 2) } else { 0 }
    
    Write-Host ""
    Write-Host "=== Load Test Results ===" -ForegroundColor $Cyan
    Write-Host "  Total Requests: $totalRequests"
    Write-Host "  Successful Requests: $($Global:SuccessCount)" -ForegroundColor $Green
    Write-Host "  Failed Requests: $($Global:ErrorCount)" -ForegroundColor $(if ($Global:ErrorCount -gt 0) { $Red } else { $Green })
    Write-Host "  Success Rate: $successRate%"
    Write-Host "  Average Response Time: ${avgResponseTime}ms"
    Write-Host "  Throughput: $throughput requests/second"
    
    if ($Global:ResponseTimes.Count -gt 0) {
        $minTime = ($Global:ResponseTimes | Measure-Object -Minimum).Minimum
        $maxTime = ($Global:ResponseTimes | Measure-Object -Maximum).Maximum
        Write-Host "  Min Response Time: ${minTime}ms"
        Write-Host "  Max Response Time: ${maxTime}ms"
    }
    
    Write-Host ""
}

# Main execution
try {
    # Check service health
    if (-not (Test-ServiceHealth)) {
        Write-Host "Some services are not healthy. Exiting..." -ForegroundColor $Red
        exit 1
    }
    
    # Authenticate
    Write-Host "Authenticating..." -ForegroundColor $Yellow
    $token = Get-AuthToken -UserServiceUrl $UserServiceUrl -Username $Username -Password $Password
    if (-not $token) {
        Write-Host "Authentication failed. Exiting..." -ForegroundColor $Red
        exit 1
    }
    
    # Warmup requests
    if ($WarmupRequests -gt 0) {
        Write-Host "Running warmup requests..." -ForegroundColor $Yellow
        for ($i = 1; $i -le $WarmupRequests; $i++) {
            $endpoint = $AnalyticsEndpoints[0]  # Use dashboard for warmup
            $warmupResult = Test-AnalyticsEndpoint -BaseUrl $AnalyticsServiceUrl -Token $token -EndpointPath $endpoint.Path -EndpointName $endpoint.Name -RequestId "warmup-$i"
            Write-Host "Warmup $i of $WarmupRequests completed: $($warmupResult.ResponseTime)ms ($($endpoint.Name))" -ForegroundColor $Cyan
        }
        
        # Reset counters after warmup
        $Global:Results = @()
        $Global:ErrorCount = 0
        $Global:SuccessCount = 0
        $Global:ResponseTimes = @()
        
        Write-Host "Warmup completed. Starting actual load test..." -ForegroundColor $Green
        Write-Host ""
    }
    
    # Main load test (simplified - no background jobs)
    $endTime = (Get-Date).AddMinutes($TestDurationMinutes)
    $requestId = 0
    
    Write-Host "Starting load test..." -ForegroundColor $Yellow
    Write-Host "Test will run until: $endTime" -ForegroundColor $Yellow
    if ($UseApiGateway) {
        Write-Host "Testing through API Gateway" -ForegroundColor $Magenta
    } else {
        Write-Host "Testing direct service access" -ForegroundColor $Magenta
    }
    Write-Host ""
    
    while ((Get-Date) -lt $endTime) {
        # Sequential requests (simpler than parallel jobs)
        for ($i = 0; $i -lt $ConcurrentUsers; $i++) {
            $requestId++
            $endpoint = Get-WeightedEndpoint -Endpoints $AnalyticsEndpoints
            
            $result = Test-AnalyticsEndpoint -BaseUrl $AnalyticsServiceUrl -Token $token -EndpointPath $endpoint.Path -EndpointName $endpoint.Name -RequestId "req-$requestId"
            
            # Progress indicator
            if ($requestId % 10 -eq 0) {
                $elapsed = [math]::Round(((Get-Date) - (Get-Date).AddMinutes(-$TestDurationMinutes + ($endTime - (Get-Date)).TotalMinutes)).TotalSeconds, 1)
                $avgResponseTime = if ($Global:ResponseTimes.Count -gt 0) { [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2) } else { 0 }
                Write-Host "Progress: $requestId requests | Avg Response: ${avgResponseTime}ms | Errors: $($Global:ErrorCount)" -ForegroundColor $Cyan
            }
        }
        
        # Small delay to prevent overwhelming the services
        Start-Sleep -Milliseconds 100
    }
    
    # Generate final report
    Generate-PerformanceReport
    
    Write-Host "Load test completed successfully!" -ForegroundColor $Green
    
} catch {
    Write-Host "Load test failed: $($_.Exception.Message)" -ForegroundColor $Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor $Red
    exit 1
}
