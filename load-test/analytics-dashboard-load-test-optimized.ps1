# Analytics Dashboard Load Test Script - Memory Optimized Version
# This script tests the microservices architecture performance for the analytics dashboard endpoint
# Usage: .\analytics-dashboard-load-test.ps1 -ConcurrentUsers 10 -TestDurationMinutes 5

param(
    [int]$ConcurrentUsers = 5,
    [int]$TestDurationMinutes = 2,
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "admin",
    [string]$Password = "admin123",
    [int]$WarmupRequests = 5,
    [int]$MaxConcurrentJobs = 15  # Increased slightly for better throughput but still safe
)

# Colors for output
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"
$Cyan = "Cyan"

Write-Host "=== Analytics Dashboard Load Test - Memory Optimized ===" -ForegroundColor $Cyan
Write-Host "Target URL: $BaseUrl/api/analytics/dashboard" -ForegroundColor $Green
Write-Host "Concurrent Users: $ConcurrentUsers" -ForegroundColor $Green
Write-Host "Test Duration: $TestDurationMinutes minutes" -ForegroundColor $Green
Write-Host "Max Concurrent Jobs: $MaxConcurrentJobs" -ForegroundColor $Green
Write-Host "Warmup Requests: $WarmupRequests" -ForegroundColor $Green
Write-Host ""

# Global variables for metrics
$Global:Results = [System.Collections.ArrayList]::new()
$Global:ErrorCount = 0
$Global:SuccessCount = 0
$Global:ResponseTimes = [System.Collections.ArrayList]::new()
$Global:TotalRequests = 0

# Adjust MaxConcurrentJobs based on ConcurrentUsers if not explicitly set
if ($MaxConcurrentJobs -eq 15 -and $ConcurrentUsers -gt 20) {
    $MaxConcurrentJobs = [math]::Min(25, [math]::Max(15, [math]::Floor($ConcurrentUsers / 2)))
    Write-Host "Adjusted MaxConcurrentJobs to $MaxConcurrentJobs based on ConcurrentUsers" -ForegroundColor $Yellow
}

# Function to authenticate and get JWT token
function Get-AuthToken {
    param([string]$BaseUrl, [string]$Username, [string]$Password)
    
    try {
        $loginPayload = @{
            usernameOrEmail = $Username
            password = $Password
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $loginPayload -TimeoutSec 30
        
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

# Helper function to convert hashtable results to proper objects for CSV export
function Convert-ResultToObject {
    param($result)
    
    return [PSCustomObject]@{
        RequestId = $result.RequestId
        Timestamp = $result.Timestamp
        ResponseTime = $result.ResponseTime
        Status = $result.Status
        DataSize = if ($result.DataSize) { $result.DataSize } else { 0 }
        Error = if ($result.Error) { $result.Error } else { "" }
    }
}

# Function to test dashboard endpoint (synchronous version)
function Test-DashboardEndpoint {
    param([string]$BaseUrl, [string]$Token, $RequestId)
    
    $headers = @{
        "Authorization" = "Bearer $Token"
        "Accept" = "application/json"
    }
    
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/dashboard" -Method GET -Headers $headers -TimeoutSec 60
        $stopwatch.Stop()
        
        $responseTime = $stopwatch.ElapsedMilliseconds
        
        # Validate response structure - simplified to reduce memory usage
        $isValid = $response -and ($response.PSObject.Properties.Count -gt 0)
        
        if ($isValid) {
            return @{
                RequestId = $RequestId
                ResponseTime = $responseTime
                Status = "Success"
                DataSize = ($response | ConvertTo-Json -Compress).Length
                Timestamp = Get-Date
            }
        } else {
            throw "Invalid response structure"
        }
    } catch {
        $stopwatch.Stop()
        return @{
            RequestId = $RequestId
            ResponseTime = $stopwatch.ElapsedMilliseconds
            Status = "Error"
            Error = $_.Exception.Message
            DataSize = 0
            Timestamp = Get-Date
        }
    }
}

# Improved load test function with job throttling
function Start-LoadTest {
    param([string]$BaseUrl, [string]$Token, [int]$ConcurrentUsers, [int]$DurationMinutes)
    
    $endTime = (Get-Date).AddMinutes($DurationMinutes)
    $requestId = 0
    $activeJobs = @{}
    $requestsPerSecond = [math]::Max(1, [math]::Floor($ConcurrentUsers / 2))
    
    Write-Host "Starting optimized load test..." -ForegroundColor $Yellow
    Write-Host "Max concurrent jobs: $MaxConcurrentJobs" -ForegroundColor $Yellow
    Write-Host "Target requests per second: $requestsPerSecond" -ForegroundColor $Yellow
    Write-Host "Test will run until: $endTime" -ForegroundColor $Yellow
    Write-Host ""
    
    while ((Get-Date) -lt $endTime) {
        # Clean up completed jobs first
        $completedJobIds = @()
        foreach ($jobId in $activeJobs.Keys) {
            $job = $activeJobs[$jobId]
            if ($job.State -eq "Completed") {
                try {
                    $result = Receive-Job $job -ErrorAction SilentlyContinue
                    Remove-Job $job -Force -ErrorAction SilentlyContinue
                    
                    if ($result) {
                        if ($result.Status -eq "Success") {
                            $Global:SuccessCount++
                            [void]$Global:ResponseTimes.Add($result.ResponseTime)
                        } else {
                            $Global:ErrorCount++
                        }
                        $resultObj = Convert-ResultToObject $result
                        [void]$Global:Results.Add($resultObj)
                    }
                } catch {
                    $Global:ErrorCount++
                }
                $completedJobIds += $jobId
            }
        }
        
        # Remove completed jobs from tracking
        foreach ($jobId in $completedJobIds) {
            $activeJobs.Remove($jobId)
        }
        
        # Start new jobs if under the limit
        $jobsToStart = [math]::Min($requestsPerSecond, ($MaxConcurrentJobs - $activeJobs.Count))
        
        for ($i = 0; $i -lt $jobsToStart; $i++) {
            $requestId++
            $Global:TotalRequests++
            
            try {
                $job = Start-Job -ScriptBlock {
                    param($BaseUrl, $Token, $RequestId)
                    
                    $headers = @{
                        "Authorization" = "Bearer $Token"
                        "Accept" = "application/json"
                    }
                    
                    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
                    
                    try {
                        $response = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/dashboard" -Method GET -Headers $headers -TimeoutSec 60
                        $stopwatch.Stop()
                        
                        return @{
                            RequestId = $RequestId
                            ResponseTime = $stopwatch.ElapsedMilliseconds
                            Status = "Success"
                            DataSize = ($response | ConvertTo-Json -Compress).Length
                            Timestamp = Get-Date
                        }
                    } catch {
                        $stopwatch.Stop()
                        return @{
                            RequestId = $RequestId
                            ResponseTime = $stopwatch.ElapsedMilliseconds
                            Status = "Error"
                            Error = $_.Exception.Message
                            DataSize = 0
                            Timestamp = Get-Date
                        }
                    }
                } -ArgumentList $BaseUrl, $Token, $requestId
                
                $activeJobs[$requestId] = $job
            } catch {
                Write-Host "Failed to start job: $($_.Exception.Message)" -ForegroundColor $Red
                $Global:ErrorCount++
            }
        }
        
        # Progress indicator
        $totalRequests = $Global:SuccessCount + $Global:ErrorCount
        if ($totalRequests % 25 -eq 0 -and $totalRequests -gt 0) {
            $avgResponseTime = if ($Global:ResponseTimes.Count -gt 0) { 
                [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2) 
            } else { 0 }
            
            $currentTime = Get-Date
            $remainingTime = $endTime - $currentTime
            $remainingMinutes = [math]::Round($remainingTime.TotalMinutes, 1)
            $activeJobCount = $activeJobs.Count
            Write-Host "Progress: $totalRequests requests | Active jobs: $activeJobCount | Avg: ${avgResponseTime}ms | Errors: $($Global:ErrorCount) | Remaining: ${remainingMinutes}min" -ForegroundColor $Cyan
        }
        
        # Rate limiting - prevent overwhelming the system
        Start-Sleep -Milliseconds 500
    }
    
    # Wait for remaining jobs with timeout
    Write-Host "Waiting for remaining $($activeJobs.Count) jobs to complete..." -ForegroundColor $Yellow
    
    $waitEndTime = (Get-Date).AddMinutes(2)  # Max 2 minute wait
    while ($activeJobs.Count -gt 0 -and (Get-Date) -lt $waitEndTime) {
        $completedJobIds = @()
        foreach ($jobId in $activeJobs.Keys) {
            $job = $activeJobs[$jobId]
            if ($job.State -eq "Completed") {
                try {
                    $result = Receive-Job $job -ErrorAction SilentlyContinue
                    Remove-Job $job -Force -ErrorAction SilentlyContinue
                    
                    if ($result) {
                        if ($result.Status -eq "Success") {
                            $Global:SuccessCount++
                            [void]$Global:ResponseTimes.Add($result.ResponseTime)
                        } else {
                            $Global:ErrorCount++
                        }
                        $resultObj = Convert-ResultToObject $result
                        [void]$Global:Results.Add($resultObj)
                    }
                } catch {
                    $Global:ErrorCount++
                }
                $completedJobIds += $jobId
            }
        }
        
        foreach ($jobId in $completedJobIds) {
            $activeJobs.Remove($jobId)
        }
        
        if ($activeJobs.Count -gt 0) {
            Start-Sleep -Milliseconds 100
        }
    }
    
    # Force cleanup any remaining jobs
    foreach ($job in $activeJobs.Values) {
        try {
            Remove-Job $job -Force -ErrorAction SilentlyContinue
        } catch {
            # Ignore cleanup errors
        }
    }
    
    Write-Host "Load test completed. Processed $($Global:SuccessCount + $Global:ErrorCount) requests." -ForegroundColor $Green
}

# Function to generate performance report (simplified)
function Generate-PerformanceReport {
    $totalRequests = $Global:SuccessCount + $Global:ErrorCount
    $successRate = if ($totalRequests -gt 0) { [math]::Round(($Global:SuccessCount / $totalRequests) * 100, 2) } else { 0 }
    
    if ($Global:ResponseTimes.Count -gt 0) {
        $avgResponseTime = [math]::Round(($Global:ResponseTimes | Measure-Object -Average).Average, 2)
        $minResponseTime = ($Global:ResponseTimes | Measure-Object -Minimum).Minimum
        $maxResponseTime = ($Global:ResponseTimes | Measure-Object -Maximum).Maximum
        
        $sortedTimes = $Global:ResponseTimes | Sort-Object
        $medianIndex = [math]::Floor($sortedTimes.Count / 2)
        $medianResponseTime = [math]::Round($sortedTimes[$medianIndex], 2)
        
        $p90Index = [math]::Floor($sortedTimes.Count * 0.9)
        $p95Index = [math]::Floor($sortedTimes.Count * 0.95)
        $p99Index = [math]::Floor($sortedTimes.Count * 0.99)
        
        $p90 = [math]::Round($sortedTimes[$p90Index], 2)
        $p95 = [math]::Round($sortedTimes[$p95Index], 2)
        $p99 = [math]::Round($sortedTimes[$p99Index], 2)
    } else {
        $avgResponseTime = $minResponseTime = $maxResponseTime = $medianResponseTime = $p90 = $p95 = $p99 = 0
    }
    
    $testDurationSeconds = $TestDurationMinutes * 60
    $throughput = if ($testDurationSeconds -gt 0) { [math]::Round($totalRequests / $testDurationSeconds, 2) } else { 0 }
    
    Write-Host ""
    Write-Host "=== MICROSERVICES ARCHITECTURE PERFORMANCE REPORT ===" -ForegroundColor $Cyan
    Write-Host ""
    Write-Host "Test Configuration:" -ForegroundColor $Yellow
    Write-Host "  Concurrent Users: $ConcurrentUsers"
    Write-Host "  Test Duration: $TestDurationMinutes minutes"
    Write-Host "  Max Concurrent Jobs: $MaxConcurrentJobs"
    Write-Host "  Target Endpoint: /api/analytics/dashboard"
    Write-Host ""
    Write-Host "Request Statistics:" -ForegroundColor $Yellow
    Write-Host "  Total Requests: $totalRequests"
    Write-Host "  Successful Requests: $($Global:SuccessCount)"
    Write-Host "  Failed Requests: $($Global:ErrorCount)"
    Write-Host "  Success Rate: ${successRate}%"
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
    
    # Performance assessment
    Write-Host "Performance Assessment:" -ForegroundColor $Yellow
    if ($avgResponseTime -lt 1000) {
        Write-Host "  [+] Good response time for microservices (< 1s)" -ForegroundColor $Green
    } elseif ($avgResponseTime -lt 2000) {
        Write-Host "  [!] Acceptable response time (1-2s)" -ForegroundColor $Yellow
    } else {
        Write-Host "  [-] Poor response time (> 2s)" -ForegroundColor $Red
    }
    
    if ($successRate -gt 95) {
        Write-Host "  [+] Excellent reliability (> 95% success rate)" -ForegroundColor $Green
    } elseif ($successRate -gt 90) {
        Write-Host "  [!] Good reliability (90-95% success rate)" -ForegroundColor $Yellow
    } else {
        Write-Host "  [-] Poor reliability (< 90% success rate)" -ForegroundColor $Red
    }
    
    # Save results
    $timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $csvPath = "analytics-microservices-load-test-results-$timestamp.csv"
    $reportPath = "analytics-microservices-performance-report-$timestamp.txt"
    
    try {
        $Global:Results | Export-Csv -Path $csvPath -NoTypeInformation
        Write-Host ""
        Write-Host "Results saved to: $csvPath" -ForegroundColor $Green
    } catch {
        Write-Host "Failed to save CSV: $($_.Exception.Message)" -ForegroundColor $Red
    }
    
    # Generate comprehensive text report
    $avgDataSize = 0
    try {
        if ($Global:Results.Count -gt 0) {
            $successfulResults = $Global:Results | Where-Object { $_.Status -eq "Success" }
            if ($successfulResults.Count -gt 0) {
                # Check if DataSize property exists
                $dataSizes = $successfulResults | Where-Object { $_.PSObject.Properties.Name -contains "DataSize" -and $_.DataSize -ne $null } | Select-Object -ExpandProperty DataSize
                if ($dataSizes.Count -gt 0) {
                    $avgDataSize = [math]::Round(($dataSizes | Measure-Object -Average).Average, 2)
                }
            }
        }
    } catch {
        Write-Host "Warning: Could not calculate average data size: $($_.Exception.Message)" -ForegroundColor $Yellow
        $avgDataSize = 0
    }
    
    $currentDateTime = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    $reportContent = @"
=== MICROSERVICES ARCHITECTURE PERFORMANCE REPORT ===
Generated: $currentDateTime

Test Configuration:
  Concurrent Users: $ConcurrentUsers
  Test Duration: $TestDurationMinutes minutes
  Max Concurrent Jobs: $MaxConcurrentJobs
  Target Endpoint: /api/analytics/dashboard
  Base URL: $BaseUrl
  Warmup Requests: $WarmupRequests

Request Statistics:
  Total Requests: $totalRequests
  Successful Requests: $($Global:SuccessCount)
  Failed Requests: $($Global:ErrorCount)
  Success Rate: ${successRate}%
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

Data Analysis:
  Average Response Data Size: $avgDataSize bytes
  
"@

    # Add error analysis if there were failures
    if ($Global:ErrorCount -gt 0) {
        $errorResults = $Global:Results | Where-Object { $_.Status -eq "Error" }
        $commonErrors = $errorResults | Group-Object -Property Error | Sort-Object Count -Descending | Select-Object -First 5
        
        $reportContent += "Error Analysis:`n"
        $reportContent += "  Total Errors: $($Global:ErrorCount)`n"
        $reportContent += "  Common Error Types:`n"
        foreach ($errorGroup in $commonErrors) {
            $reportContent += "    - $($errorGroup.Name): $($errorGroup.Count) occurrences`n"
        }
        $reportContent += "`n"
    }

    $reportContent += @"
Performance Assessment:
"@

    # Add performance assessment
    if ($avgResponseTime -lt 1000) {
        $reportContent += "  [+] Good response time for microservices (< 1s)`n"
    } elseif ($avgResponseTime -lt 2000) {
        $reportContent += "  [!] Acceptable response time (1-2s)`n"
    } else {
        $reportContent += "  [-] Poor response time (> 2s)`n"
    }
    
    if ($successRate -gt 95) {
        $reportContent += "  [+] Excellent reliability (> 95% success rate)`n"
    } elseif ($successRate -gt 90) {
        $reportContent += "  [!] Good reliability (90-95% success rate)`n"
    } else {
        $reportContent += "  [-] Poor reliability (< 90% success rate)`n"
    }

    $reportContent += @"

Architecture Notes:
  - This test represents microservices architecture performance
  - Data aggregation happens across multiple services via Feign clients
  - Inter-service communication adds latency overhead
  - Each service runs in separate containers with individual databases
  - JWT tokens propagated across service boundaries
  - Circuit breakers provide fault tolerance

Microservices Characteristics:
  - Analytics Service orchestrates calls to User, Book, and Transaction services
  - Each service has independent H2 database
  - Service discovery via Eureka
  - API Gateway routing adds routing overhead
  - Distributed tracing via Zipkin (if enabled)
  - Individual service scaling possible

Test Environment:
  - Spring Boot 3.3.0 microservices
  - Docker containerized deployment
  - H2 in-memory databases per service
  - JWT authentication with token propagation
  - Feign clients for inter-service communication

Memory Optimization Applied:
  - Job throttling with max $MaxConcurrentJobs concurrent jobs
  - ArrayList for better memory management
  - Explicit garbage collection
  - Proper job cleanup and resource management

Comparison Baseline:
  - Compare these results with monolithic architecture
  - Expected higher latency due to network overhead
  - Better scalability and fault isolation
  - Distributed system complexity vs single deployment

Files Generated:
  - Detailed CSV results: $csvPath
  - Performance report: $reportPath

=== END OF REPORT ===
"@

    # Write report to file
    try {
        $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
        Write-Host "Performance report saved to: $reportPath" -ForegroundColor $Green
    } catch {
        Write-Host "Failed to save report: $($_.Exception.Message)" -ForegroundColor $Red
    }
    
    return @{
        TotalRequests = $totalRequests
        SuccessfulRequests = $Global:SuccessCount
        SuccessRate = $successRate
        AverageResponseTime = $avgResponseTime
        Throughput = $throughput
    }
}

# Main execution
try {
    # Force garbage collection to start clean
    [GC]::Collect()
    
    Write-Host "Authenticating..." -ForegroundColor $Yellow
    $token = Get-AuthToken -BaseUrl $BaseUrl -Username $Username -Password $Password
    
    if (-not $token) {
        Write-Host "Failed to authenticate. Exiting." -ForegroundColor $Red
        exit 1
    }
    
    # Quick warmup (simplified)
    if ($WarmupRequests -gt 0) {
        Write-Host "Running $WarmupRequests warmup requests..." -ForegroundColor $Yellow
        for ($i = 1; $i -le $WarmupRequests; $i++) {
            $warmupResult = Test-DashboardEndpoint -BaseUrl $BaseUrl -Token $token -RequestId "warmup-$i"
            $warmupTime = $warmupResult.ResponseTime
            Write-Host "Warmup ${i}: ${warmupTime}ms" -ForegroundColor $Cyan
        }
        Write-Host "Warmup completed." -ForegroundColor $Green
        Write-Host ""
        
        # Reset for actual test
        $Global:Results = [System.Collections.ArrayList]::new()
        $Global:ErrorCount = 0
        $Global:SuccessCount = 0
        $Global:ResponseTimes = [System.Collections.ArrayList]::new()
    }
    
    # Run the load test
    Start-LoadTest -BaseUrl $BaseUrl -Token $token -ConcurrentUsers $ConcurrentUsers -DurationMinutes $TestDurationMinutes
    
    # Generate report
    $summary = Generate-PerformanceReport
    
    Write-Host ""
    Write-Host "Load test completed successfully!" -ForegroundColor $Green
    Write-Host "Files generated:" -ForegroundColor $Cyan
    Write-Host "  - CSV data: Check the generated CSV file for detailed per-request results" -ForegroundColor $Cyan
    Write-Host "  - Report: Check the generated TXT file for complete performance analysis" -ForegroundColor $Cyan
    Write-Host "This microservices data can be compared with monolithic architecture performance." -ForegroundColor $Cyan
    
} catch {
    Write-Host "Load test failed: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
} finally {
    # Cleanup
    Get-Job | Remove-Job -Force -ErrorAction SilentlyContinue
    [GC]::Collect()
}
