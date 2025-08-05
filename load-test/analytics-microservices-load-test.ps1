# Analytics Microservices Load Test Script
# This script tests the microservices architecture performance for the analytics service API
# Usage: .\analytics-microservices-load-test.ps1 -ConcurrentUsers 10 -TestDurationMinutes 5

param(
    [int]$ConcurrentUsers = 10,
    [int]$TestDurationMinutes = 2,
    [string]$UserServiceUrl = "http://localhost:8081",
    [string]$AnalyticsServiceUrl = "http://localhost:8084",
    [string]$ApiGatewayUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [int]$WarmupRequests = 5,
    [switch]$UseApiGateway = $false,
    [switch]$TestAllEndpoints = $false
)

# Colors for output
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"
$Magenta = "Magenta"

Write-Host "=== Analytics Microservices Load Test ===" -ForegroundColor $Cyan
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
$Global:ServiceCalls = @{}
$Global:InterServiceLatency = @()

# Analytics endpoints to test
$AnalyticsEndpoints = @(
    @{
        Name = "Dashboard"
        Path = "/api/analytics/dashboard"
        Description = "Main dashboard with aggregated analytics"
        Weight = 5  # Higher weight = more frequent testing
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
        Name = "Inventory"
        Path = "/api/analytics/inventory"
        Description = "Inventory management analytics"
        Weight = 1
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

# Function to test analytics endpoint
function Test-AnalyticsEndpoint {
    param(
        [string]$BaseUrl, 
        [string]$Token, 
        [string]$EndpointPath, 
        [string]$EndpointName,
        [int]$RequestId
    )
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Accept" = "application/json"
    }
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $serviceCallCount = 0
    
    try {
        $url = if ($UseApiGateway) { "$ApiGatewayUrl$EndpointPath" } else { "$BaseUrl$EndpointPath" }
        $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers -TimeoutSec 60
        $stopwatch.Stop()
        
        $responseTime = $stopwatch.ElapsedMilliseconds
        
        # Validate response structure based on endpoint
        $isValid = $false
        $dataPoints = 0
        $executionTime = 0
        
        switch ($EndpointName) {
            "Dashboard" {
                $isValid = $response.dashboard -and $response.metadata
                if ($isValid) {
                    $dataPoints = 3  # userAnalytics, bookAnalytics, transactionAnalytics
                    $executionTime = $response.metadata.executionTimeMs
                    if ($response.metadata.serviceCalls) {
                        $serviceCallCount = $response.metadata.serviceCalls.Count
                        $Global:ServiceCalls[$EndpointName] = $response.metadata.serviceCalls
                    }
                }
            }
            "Summary" {
                $isValid = $response.summary -and $response.metadata
                if ($isValid) {
                    $dataPoints = 4  # Basic summary stats
                    $executionTime = $response.metadata.executionTimeMs
                }
            }
            "Users" {
                $isValid = $response.userAnalytics -and $response.metadata
                if ($isValid) {
                    $dataPoints = ($response.userAnalytics | Get-Member -Type NoteProperty).Count
                    $executionTime = $response.metadata.executionTimeMs
                }
            }
            "Books" {
                $isValid = $response.bookAnalytics -and $response.metadata
                if ($isValid) {
                    $dataPoints = ($response.bookAnalytics | Get-Member -Type NoteProperty).Count
                    $executionTime = $response.metadata.executionTimeMs
                }
            }
            "Transactions" {
                $isValid = $response.transactionAnalytics -and $response.metadata
                if ($isValid) {
                    $dataPoints = ($response.transactionAnalytics | Get-Member -Type NoteProperty).Count
                    $executionTime = $response.metadata.executionTimeMs
                }
            }
            "Inventory" {
                $isValid = $response.inventoryAnalytics -and $response.metadata
                if ($isValid) {
                    $dataPoints = ($response.inventoryAnalytics | Get-Member -Type NoteProperty).Count
                    $executionTime = $response.metadata.executionTimeMs
                }
            }
            "Health" {
                $isValid = $response.status -or $response.health
                if ($isValid) {
                    $dataPoints = 1
                    $executionTime = $responseTime  # Health check execution time
                }
            }
        }
        
        if ($isValid) {
            $Global:SuccessCount++
            $Global:ResponseTimes += $responseTime
            
            # Track inter-service communication latency
            if ($executionTime -gt 0 -and $executionTime -lt $responseTime) {
                $networkLatency = $responseTime - $executionTime
                $Global:InterServiceLatency += $networkLatency
            }
            
            $result = [PSCustomObject]@{
                RequestId = $RequestId
                Timestamp = Get-Date
                Endpoint = $EndpointName
                EndpointPath = $EndpointPath
                ResponseTime = $responseTime
                Status = "Success"
                DataSize = ($response | ConvertTo-Json -Depth 10).Length
                DataPoints = $dataPoints
                ExecutionTime = $executionTime
                ServiceCalls = $serviceCallCount
                NetworkLatency = if ($executionTime -gt 0 -and $executionTime -lt $responseTime) { $responseTime - $executionTime } else { 0 }
                Architecture = if ($UseApiGateway) { "Gateway" } else { "Direct" }
            }
            
            $Global:Results += $result
            return $result
        } else {
            throw "Invalid response structure for $EndpointName"
        }
    } catch {
        $stopwatch.Stop()
        $Global:ErrorCount++
        
        $result = [PSCustomObject]@{
            RequestId = $RequestId
            Timestamp = Get-Date
            Endpoint = $EndpointName
            EndpointPath = $EndpointPath
            ResponseTime = $stopwatch.ElapsedMilliseconds
            Status = "Error"
            Error = $_.Exception.Message
            DataSize = 0
            DataPoints = 0
            ExecutionTime = 0
            ServiceCalls = 0
            NetworkLatency = 0
            Architecture = if ($UseApiGateway) { "Gateway" } else { "Direct" }
        }
        
        $Global:Results += $result
        return $result
    }
}

# Function to select endpoint based on weight
function Get-WeightedEndpoint {
    param($Endpoints)
    
    if ($TestAllEndpoints) {
        # Return random endpoint when testing all
        return $Endpoints[(Get-Random -Maximum $Endpoints.Count)]
    } else {
        # Use weighted selection - focus on dashboard and summary
        $totalWeight = ($Endpoints | Measure-Object -Property Weight -Sum).Sum
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
    }
}

# Function to run concurrent load test
function Start-LoadTest {
    param([string]$UserServiceUrl, [string]$AnalyticsServiceUrl, [string]$Token, [int]$ConcurrentUsers, [int]$DurationMinutes)
    
    $endTime = (Get-Date).AddMinutes($DurationMinutes)
    $requestId = 0
    $jobs = @()
    
    Write-Host "Starting microservices load test..." -ForegroundColor $Yellow
    Write-Host "Test will run until: $endTime" -ForegroundColor $Yellow
    if ($UseApiGateway) {
        Write-Host "Testing through API Gateway (simulates real-world usage)" -ForegroundColor $Magenta
    } else {
        Write-Host "Testing direct service access (microservices baseline)" -ForegroundColor $Magenta
    }
    Write-Host ""
    
    while ((Get-Date) -lt $endTime) {
        # Start concurrent requests
        for ($i = 0; $i -lt $ConcurrentUsers; $i++) {
            $requestId++
            $endpoint = Get-WeightedEndpoint -Endpoints $AnalyticsEndpoints
            
            $job = Start-Job -ScriptBlock {
                param($AnalyticsServiceUrl, $ApiGatewayUrl, $Token, $RequestId, $Endpoint, $UseApiGateway)
                
                # Re-define the function in the job scope
                function Test-AnalyticsEndpoint {
                    param(
                        [string]$BaseUrl, 
                        [string]$Token, 
                        [string]$EndpointPath, 
                        [string]$EndpointName,
                        [int]$RequestId,
                        [bool]$UseApiGateway,
                        [string]$ApiGatewayUrl
                    )
                    
                    $headers = @{
                        "Authorization" = "Bearer $Token"
                        "Accept" = "application/json"
                    }
                    
                    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
                    $serviceCallCount = 0
                    
                    try {
                        $url = if ($UseApiGateway) { "$ApiGatewayUrl$EndpointPath" } else { "$BaseUrl$EndpointPath" }
                        $response = Invoke-RestMethod -Uri $url -Method GET -Headers $headers -TimeoutSec 60
                        $stopwatch.Stop()
                        
                        $responseTime = $stopwatch.ElapsedMilliseconds
                        
                        # Basic validation
                        $isValid = $response -and ($response.GetType().Name -eq "PSCustomObject" -or $response.GetType().Name -eq "Hashtable")
                        $dataPoints = 0
                        $executionTime = 0
                        
                        if ($isValid) {
                            $dataPoints = if ($response.metadata) { 1 } else { 0 }
                            $executionTime = if ($response.metadata -and $response.metadata.executionTimeMs) { $response.metadata.executionTimeMs } else { 0 }
                            
                            if ($response.metadata -and $response.metadata.serviceCalls) {
                                $serviceCallCount = $response.metadata.serviceCalls.Count
                            }
                        }
                        
                        if ($isValid) {
                            return [PSCustomObject]@{
                                RequestId = $RequestId
                                Timestamp = Get-Date
                                Endpoint = $EndpointName
                                EndpointPath = $EndpointPath
                                ResponseTime = $responseTime
                                Status = "Success"
                                DataSize = ($response | ConvertTo-Json -Depth 10).Length
                                DataPoints = $dataPoints
                                ExecutionTime = $executionTime
                                ServiceCalls = $serviceCallCount
                                NetworkLatency = if ($executionTime -gt 0 -and $executionTime -lt $responseTime) { $responseTime - $executionTime } else { 0 }
                                Architecture = if ($UseApiGateway) { "Gateway" } else { "Direct" }
                            }
                        } else {
                            throw "Invalid response structure for $EndpointName"
                        }
                    } catch {
                        $stopwatch.Stop()
                        return [PSCustomObject]@{
                            RequestId = $RequestId
                            Timestamp = Get-Date
                            Endpoint = $EndpointName
                            EndpointPath = $EndpointPath
                            ResponseTime = $stopwatch.ElapsedMilliseconds
                            Status = "Error"
                            Error = $_.Exception.Message
                            DataSize = 0
                            DataPoints = 0
                            ExecutionTime = 0
                            ServiceCalls = 0
                            NetworkLatency = 0
                            Architecture = if ($UseApiGateway) { "Gateway" } else { "Direct" }
                        }
                    }
                }
                
                return Test-AnalyticsEndpoint -BaseUrl $AnalyticsServiceUrl -Token $Token -EndpointPath $Endpoint.Path -EndpointName $Endpoint.Name -RequestId $RequestId -UseApiGateway $UseApiGateway -ApiGatewayUrl $ApiGatewayUrl
                
            } -ArgumentList $AnalyticsServiceUrl, $ApiGatewayUrl, $Token, $requestId, $endpoint, $UseApiGateway
            
            $jobs += $job
        }
        
        # Wait for jobs to complete and collect results
        $completedJobs = $jobs | Where-Object { $_.State -eq "Completed" }
        foreach ($job in $completedJobs) {
            $result = Receive-Job $job
            Remove-Job $job
            
            if ($result.Status -eq "Success") {
                $Global:SuccessCount++
                $Global:ResponseTimes += $result.ResponseTime
                
                # Track inter-service latency
                if ($result.NetworkLatency -gt 0) {
                    $Global:InterServiceLatency += $result.NetworkLatency
                }
            } else {
                $Global:ErrorCount++
            }
            
            $Global:Results += $result
            
            # Real-time progress indicator
            $totalRequests = $Global:SuccessCount + $Global:ErrorCount
            if ($totalRequests % 10 -eq 0) {
                $avgResponseTime = if ($Global:ResponseTimes.Count -gt 0) { 
                    [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2) 
                } else { 0 }
                
                $avgNetworkLatency = if ($Global:InterServiceLatency.Count -gt 0) {
                    [math]::Round(($Global:InterServiceLatency | Measure-Object -Average).Average, 2)
                } else { 0 }
                
                Write-Host "Progress: $totalRequests requests | Avg Response: ${avgResponseTime}ms | Network Latency: ${avgNetworkLatency}ms | Errors: $($Global:ErrorCount)" -ForegroundColor $Cyan
            }
        }
        
        # Remove completed jobs from the list
        $jobs = $jobs | Where-Object { $_.State -ne "Completed" }
        
        # Small delay to prevent overwhelming the services
        Start-Sleep -Milliseconds 50
    }
    
    # Wait for any remaining jobs to complete
    Write-Host "Waiting for remaining requests to complete..." -ForegroundColor $Yellow
    $jobs | Wait-Job | ForEach-Object {
        $result = Receive-Job $_
        Remove-Job $_
        
        if ($result.Status -eq "Success") {
            $Global:SuccessCount++
            $Global:ResponseTimes += $result.ResponseTime
            
            if ($result.NetworkLatency -gt 0) {
                $Global:InterServiceLatency += $result.NetworkLatency
            }
        } else {
            $Global:ErrorCount++
        }
        
        $Global:Results += $result
    }
}

# Function to generate microservices performance report
function Generate-MicroservicesPerformanceReport {
    $totalRequests = $Global:SuccessCount + $Global:ErrorCount
    $successRate = if ($totalRequests -gt 0) { [math]::Round(($Global:SuccessCount / $totalRequests) * 100, 2) } else { 0 }
    
    if ($Global:ResponseTimes.Count -gt 0) {
        $avgResponseTime = [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2)
        $minResponseTime = ($Global:ResponseTimes | Measure-Object -Minimum).Minimum
        $maxResponseTime = ($Global:ResponseTimes | Measure-Object -Maximum).Maximum
        $medianResponseTime = [math]::Round(($Global:ResponseTimes | Sort-Object)[[math]::Floor($Global:ResponseTimes.Count / 2)], 2)
        
        # Calculate percentiles
        $sortedTimes = $Global:ResponseTimes | Sort-Object
        $p90 = [math]::Round($sortedTimes[[math]::Floor($sortedTimes.Count * 0.9)], 2)
        $p95 = [math]::Round($sortedTimes[[math]::Floor($sortedTimes.Count * 0.95)], 2)
        $p99 = [math]::Round($sortedTimes[[math]::Floor($sortedTimes.Count * 0.99)], 2)
    } else {
        $avgResponseTime = $minResponseTime = $maxResponseTime = $medianResponseTime = $p90 = $p95 = $p99 = 0
    }
    
    # Calculate network latency metrics
    if ($Global:InterServiceLatency.Count -gt 0) {
        $avgNetworkLatency = [math]::Round(($Global:InterServiceLatency | Measure-Object -Average).Average, 2)
        $maxNetworkLatency = ($Global:InterServiceLatency | Measure-Object -Maximum).Maximum
        $minNetworkLatency = ($Global:InterServiceLatency | Measure-Object -Minimum).Minimum
    } else {
        $avgNetworkLatency = $maxNetworkLatency = $minNetworkLatency = 0
    }
    
    # Calculate throughput
    $testDurationSeconds = $TestDurationMinutes * 60
    $throughput = if ($testDurationSeconds -gt 0) { [math]::Round($totalRequests / $testDurationSeconds, 2) } else { 0 }
    
    # Endpoint statistics
    $endpointStats = $Global:Results | Where-Object { $_.Status -eq "Success" } | Group-Object Endpoint | ForEach-Object {
        $endpointResults = $_.Group
        [PSCustomObject]@{
            Endpoint = $_.Name
            RequestCount = $endpointResults.Count
            AvgResponseTime = [math]::Round(($endpointResults | Measure-Object -Property ResponseTime -Average).Average, 2)
            AvgDataSize = [math]::Round(($endpointResults | Measure-Object -Property DataSize -Average).Average, 2)
            AvgServiceCalls = [math]::Round(($endpointResults | Measure-Object -Property ServiceCalls -Average).Average, 2)
            AvgExecutionTime = [math]::Round(($endpointResults | Measure-Object -Property ExecutionTime -Average).Average, 2)
            AvgNetworkLatency = [math]::Round(($endpointResults | Measure-Object -Property NetworkLatency -Average).Average, 2)
        }
    }
    
    Write-Host ""
    Write-Host "=== MICROSERVICES ARCHITECTURE PERFORMANCE REPORT ===" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "Test Configuration:" -ForegroundColor $Yellow
    Write-Host "  Architecture: Microservices ($( if ($UseApiGateway) { 'via API Gateway' } else { 'Direct Service Access' } ))"
    Write-Host "  Concurrent Users: $ConcurrentUsers"
    Write-Host "  Test Duration: $TestDurationMinutes minutes"
    Write-Host "  User Service: $UserServiceUrl"
    Write-Host "  Analytics Service: $AnalyticsServiceUrl"
    if ($UseApiGateway) {
        Write-Host "  API Gateway: $ApiGatewayUrl"
    }
    Write-Host "  Test All Endpoints: $TestAllEndpoints"
    Write-Host ""
    Write-Host "Request Statistics:" -ForegroundColor $Yellow
    Write-Host "  Total Requests: $totalRequests"
    Write-Host "  Successful Requests: $($Global:SuccessCount)"
    Write-Host "  Failed Requests: $($Global:ErrorCount)"
    Write-Host "  Success Rate: $successRate%"
    Write-Host "  Throughput: $throughput requests/second"
    Write-Host ""
    Write-Host "Response Time Analysis:" -ForegroundColor $Yellow
    Write-Host "  Average Response Time: ${avgResponseTime}ms"
    Write-Host "  Median Response Time: ${medianResponseTime}ms"
    Write-Host "  Minimum Response Time: ${minResponseTime}ms"
    Write-Host "  Maximum Response Time: ${maxResponseTime}ms"
    Write-Host ""
    Write-Host "Response Time Percentiles:" -ForegroundColor $Yellow
    Write-Host "  90th Percentile: ${p90}ms"
    Write-Host "  95th Percentile: ${p95}ms"
    Write-Host "  99th Percentile: ${p99}ms"
    Write-Host ""
    
    # Microservices-specific metrics
    Write-Host "Microservices Metrics:" -ForegroundColor $Yellow
    Write-Host "  Average Network Latency: ${avgNetworkLatency}ms"
    Write-Host "  Min Network Latency: ${minNetworkLatency}ms"
    Write-Host "  Max Network Latency: ${maxNetworkLatency}ms"
    if ($avgResponseTime -gt 0 -and $avgNetworkLatency -gt 0) {
        $networkOverhead = [math]::Round(($avgNetworkLatency / $avgResponseTime) * 100, 2)
        Write-Host "  Network Overhead: $networkOverhead% of total response time"
    }
    Write-Host ""
    
    # Endpoint performance breakdown
    if ($endpointStats.Count -gt 0) {
        Write-Host "Endpoint Performance Breakdown:" -ForegroundColor $Yellow
        $endpointStats | Sort-Object RequestCount -Descending | ForEach-Object {
            Write-Host "  $($_.Endpoint):"
            Write-Host "    Requests: $($_.RequestCount)"
            Write-Host "    Avg Response Time: $($_.AvgResponseTime)ms"
            Write-Host "    Avg Service Calls: $($_.AvgServiceCalls)"
            Write-Host "    Avg Execution Time: $($_.AvgExecutionTime)ms"
            Write-Host "    Avg Network Latency: $($_.AvgNetworkLatency)ms"
            Write-Host "    Avg Data Size: $($_.AvgDataSize) bytes"
            Write-Host ""
        }
    }
    
    # Performance assessment
    Write-Host "Performance Assessment:" -ForegroundColor $Yellow
    
    # Response time assessment
    if ($avgResponseTime -lt 1000) {
        Write-Host "  [+] Excellent response time (< 1s)" -ForegroundColor $Green
    } elseif ($avgResponseTime -lt 2000) {
        Write-Host "  [!] Good response time (1-2s)" -ForegroundColor $Yellow
    } elseif ($avgResponseTime -lt 5000) {
        Write-Host "  [!] Acceptable response time (2-5s)" -ForegroundColor $Yellow
    } else {
        Write-Host "  [-] Poor response time (> 5s)" -ForegroundColor $Red
    }
    
    # Network latency assessment
    if ($avgNetworkLatency -lt 100) {
        Write-Host "  [+] Excellent inter-service communication (< 100ms)" -ForegroundColor $Green
    } elseif ($avgNetworkLatency -lt 500) {
        Write-Host "  [!] Good inter-service communication (100-500ms)" -ForegroundColor $Yellow
    } else {
        Write-Host "  [-] High inter-service latency (> 500ms)" -ForegroundColor $Red
    }
    
    # Reliability assessment
    if ($successRate -gt 95) {
        Write-Host "  [+] Excellent reliability (> 95% success rate)" -ForegroundColor $Green
    } elseif ($successRate -gt 90) {
        Write-Host "  [!] Good reliability (90-95% success rate)" -ForegroundColor $Yellow
    } else {
        Write-Host "  [-] Poor reliability (< 90% success rate)" -ForegroundColor $Red
    }
    
    # Architecture-specific insights
    Write-Host ""
    Write-Host "Microservices Architecture Insights:" -ForegroundColor $Yellow
    if ($UseApiGateway) {
        Write-Host "  - API Gateway adds routing and security overhead"
        Write-Host "  - Single entry point provides better monitoring and control"
        Write-Host "  - Gateway can become a bottleneck under high load"
    } else {
        Write-Host "  - Direct service access eliminates gateway overhead"
        Write-Host "  - Better performance but requires service discovery"
        Write-Host "  - Client-side load balancing responsibility"
    }
    
    if ($avgNetworkLatency -gt 0) {
        Write-Host "  - Inter-service communication adds ${avgNetworkLatency}ms average overhead"
        Write-Host "  - Network latency varies based on service deployment"
        Write-Host "  - Consider service collocation for performance optimization"
    }
    
    # Save detailed results to CSV
    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $architecture = if ($UseApiGateway) { "microservices-gateway" } else { "microservices-direct" }
    $csvPath = "analytics-$architecture-load-test-results-$timestamp.csv"
    $reportPath = "analytics-$architecture-performance-report-$timestamp.txt"
    
    $Global:Results | Export-Csv -Path $csvPath -NoTypeInformation
    
    # Generate text report content
    $reportContent = @"
=== MICROSERVICES ARCHITECTURE PERFORMANCE REPORT ===
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

Test Configuration:
  Architecture: Microservices ($( if ($UseApiGateway) { 'via API Gateway' } else { 'Direct Service Access' } ))
  Concurrent Users: $ConcurrentUsers
  Test Duration: $TestDurationMinutes minutes
  User Service: $UserServiceUrl
  Analytics Service: $AnalyticsServiceUrl
  $( if ($UseApiGateway) { "API Gateway: $ApiGatewayUrl" } else { "" } )
  Test All Endpoints: $TestAllEndpoints

Request Statistics:
  Total Requests: $totalRequests
  Successful Requests: $($Global:SuccessCount)
  Failed Requests: $($Global:ErrorCount)
  Success Rate: $successRate%
  Throughput: $throughput requests/second

Response Time Analysis:
  Average Response Time: ${avgResponseTime}ms
  Median Response Time: ${medianResponseTime}ms
  Minimum Response Time: ${minResponseTime}ms
  Maximum Response Time: ${maxResponseTime}ms

Response Time Percentiles:
  90th Percentile: ${p90}ms
  95th Percentile: ${p95}ms
  99th Percentile: ${p99}ms

Microservices Metrics:
  Average Network Latency: ${avgNetworkLatency}ms
  Min Network Latency: ${minNetworkLatency}ms
  Max Network Latency: ${maxNetworkLatency}ms
  Network Overhead: $( if ($avgResponseTime -gt 0 -and $avgNetworkLatency -gt 0) { [math]::Round(($avgNetworkLatency / $avgResponseTime) * 100, 2) } else { 0 } )% of total response time

"@

    # Add endpoint breakdown
    if ($endpointStats.Count -gt 0) {
        $reportContent += "Endpoint Performance Breakdown:`n"
        $endpointStats | Sort-Object RequestCount -Descending | ForEach-Object {
            $reportContent += "  $($_.Endpoint):`n"
            $reportContent += "    Requests: $($_.RequestCount)`n"
            $reportContent += "    Avg Response Time: $($_.AvgResponseTime)ms`n"
            $reportContent += "    Avg Service Calls: $($_.AvgServiceCalls)`n"
            $reportContent += "    Avg Execution Time: $($_.AvgExecutionTime)ms`n"
            $reportContent += "    Avg Network Latency: $($_.AvgNetworkLatency)ms`n"
            $reportContent += "    Avg Data Size: $($_.AvgDataSize) bytes`n`n"
        }
    }

    $reportContent += @"

Architecture Notes:
  - Microservices architecture with Spring Cloud framework
  - Service discovery via Eureka Server
  - JWT-based authentication across services
  - H2 databases per service (database-per-service pattern)
  - $( if ($UseApiGateway) { "API Gateway routing adds overhead but provides centralized control" } else { "Direct service access for optimal performance" } )
  - Inter-service communication via REST APIs
  - Resilience patterns with circuit breakers

Performance Characteristics:
  - Network latency is significant factor in microservices
  - Service decomposition provides scalability benefits
  - Individual service optimization possible
  - Monitoring complexity increases with service count

Test Environment:
  - Spring Boot 3.3.0 microservices
  - Eureka service discovery
  - Spring Cloud Gateway (if enabled)
  - JWT authentication
  - Docker containerized services

Files Generated:
  - Detailed CSV results: $csvPath
  - Performance report: $reportPath

=== END OF REPORT ===
"@

    # Write report to file
    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
    
    Write-Host ""
    Write-Host "Detailed results saved to: $csvPath" -ForegroundColor $Green
    Write-Host "Performance report saved to: $reportPath" -ForegroundColor $Green
    
    # Return summary for potential comparison
    return @{
        Architecture = if ($UseApiGateway) { "Microservices-Gateway" } else { "Microservices-Direct" }
        TotalRequests = $totalRequests
        SuccessfulRequests = $Global:SuccessCount
        FailedRequests = $Global:ErrorCount
        SuccessRate = $successRate
        AverageResponseTime = $avgResponseTime
        MedianResponseTime = $medianResponseTime
        P90ResponseTime = $p90
        P95ResponseTime = $p95
        P99ResponseTime = $p99
        Throughput = $throughput
        MinResponseTime = $minResponseTime
        MaxResponseTime = $maxResponseTime
        AverageNetworkLatency = $avgNetworkLatency
        NetworkOverhead = if ($avgResponseTime -gt 0 -and $avgNetworkLatency -gt 0) { [math]::Round(($avgNetworkLatency / $avgResponseTime) * 100, 2) } else { 0 }
        EndpointCount = $endpointStats.Count
    }
}

# Function to check service health before testing
function Test-ServiceHealth {
    param([string]$UserServiceUrl, [string]$AnalyticsServiceUrl)
    
    Write-Host "Checking service health..." -ForegroundColor $Yellow
    
    $services = @(
        @{ Name = "User Service"; Url = "$UserServiceUrl/actuator/health" },
        @{ Name = "Analytics Service"; Url = "$AnalyticsServiceUrl/actuator/health" }
    )
    
    if ($UseApiGateway) {
        $services += @{ Name = "API Gateway"; Url = "$ApiGatewayUrl/actuator/health" }
    }
    
    $allHealthy = $true
    
    foreach ($service in $services) {
        try {
            $response = Invoke-RestMethod -Uri $service.Url -Method GET -TimeoutSec 10
            if ($response.status -eq "UP") {
                Write-Host "  [+] $($service.Name): Healthy" -ForegroundColor $Green
            } else {
                Write-Host "  [!] $($service.Name): $($response.status)" -ForegroundColor $Yellow
                $allHealthy = $false
            }
        } catch {
            Write-Host "  [-] $($service.Name): Unavailable - $($_.Exception.Message)" -ForegroundColor $Red
            $allHealthy = $false
        }
    }
    
    if (-not $allHealthy) {
        Write-Host ""
        Write-Host "Warning: Some services are not healthy. This may affect test results." -ForegroundColor $Yellow
        Write-Host "Continue anyway? (y/N): " -NoNewline -ForegroundColor $Yellow
        $continue = Read-Host
        if ($continue -ne "y" -and $continue -ne "Y") {
            Write-Host "Test cancelled." -ForegroundColor $Red
            exit 1
        }
    }
    
    Write-Host ""
}

# Main execution
try {
    # Check service health
    Test-ServiceHealth -UserServiceUrl $UserServiceUrl -AnalyticsServiceUrl $AnalyticsServiceUrl
    
    # Get authentication token
    Write-Host "Authenticating..." -ForegroundColor $Yellow
    $token = Get-AuthToken -UserServiceUrl $UserServiceUrl -Username $Username -Password $Password
    
    if (-not $token) {
        Write-Host "Failed to authenticate. Exiting." -ForegroundColor $Red
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
        $Global:InterServiceLatency = @()
        Write-Host "Warmup completed. Starting actual load test..." -ForegroundColor $Green
        Write-Host ""
    }
    
    # Run the load test
    Start-LoadTest -UserServiceUrl $UserServiceUrl -AnalyticsServiceUrl $AnalyticsServiceUrl -Token $token -ConcurrentUsers $ConcurrentUsers -DurationMinutes $TestDurationMinutes
    
    # Generate performance report
    $summary = Generate-MicroservicesPerformanceReport
    
    Write-Host ""
    Write-Host "Microservices load test completed successfully!" -ForegroundColor $Green
    Write-Host "Files generated:" -ForegroundColor $Cyan
    Write-Host "  - CSV data: Check the generated CSV file for detailed per-request results" -ForegroundColor $Cyan
    Write-Host "  - Report: Check the generated TXT file for complete performance analysis" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor $Cyan
    Write-Host "  1. Compare results with monolithic architecture performance" -ForegroundColor $Cyan
    Write-Host "  2. Test with API Gateway enabled/disabled to see routing overhead" -ForegroundColor $Cyan
    Write-Host "  3. Run tests with different concurrent user loads" -ForegroundColor $Cyan
    Write-Host "  4. Monitor individual service performance in production" -ForegroundColor $Cyan
    
} catch {
    Write-Host "Load test failed: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
}
