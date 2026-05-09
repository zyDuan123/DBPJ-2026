param(
    [int]$Port = 18080
)

$ErrorActionPreference = "Stop"
$base = "http://localhost:$Port/api/v1"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$backend = Join-Path $root "backend/activity"
$jar = Join-Path $backend "target/activity-0.0.1-SNAPSHOT.jar"

function Assert-Code($response, $name) {
    if ($null -eq $response -or $response.code -ne 20000) {
        $json = $response | ConvertTo-Json -Depth 10
        throw "$name failed: $json"
    }
    Write-Host "PASS $name"
}

function Invoke-Api($method, $path, $token = $null, $body = $null) {
    $headers = @{}
    if ($token) {
        $headers.Authorization = "Bearer $token"
    }
    $args = @{
        Method = $method
        Uri = "$base$path"
        Headers = $headers
    }
    if ($null -ne $body) {
        $args.ContentType = "application/json; charset=utf-8"
        $args.Body = ($body | ConvertTo-Json -Depth 10)
    }
    Invoke-RestMethod @args
}

if (!(Test-Path $jar)) {
    throw "Jar not found: $jar"
}

$process = Start-Process -FilePath (Get-Command java).Source `
    -ArgumentList @("-jar", "target/activity-0.0.1-SNAPSHOT.jar", "--server.port=$Port") `
    -WorkingDirectory $backend `
    -WindowStyle Hidden `
    -PassThru

try {
    $student = $null
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        try {
            $student = Invoke-Api POST "/auth/login" $null @{ username = "20230001"; password = "123456" }
            break
        } catch {
            if ($i -eq 29) { throw }
        }
    }
    Assert-Code $student "POST /auth/login student"
    $studentToken = $student.data.token

    $organizer = Invoke-Api POST "/auth/login" $null @{ username = "13800000002"; password = "123456" }
    Assert-Code $organizer "POST /auth/login organizer"
    $organizerToken = $organizer.data.token

    $admin = Invoke-Api POST "/auth/login" $null @{ username = "13800000003"; password = "123456" }
    Assert-Code $admin "POST /auth/login admin"
    $adminToken = $admin.data.token

    Assert-Code (Invoke-Api GET "/auth/me" $studentToken) "GET /auth/me"

    $campuses = Invoke-Api GET "/campuses" $studentToken
    Assert-Code $campuses "GET /campuses"

    Assert-Code (Invoke-Api POST "/campuses" $adminToken @{ campusName = "Smoke Campus"; location = "Smoke Location" }) "POST /campuses"
    $campusList = Invoke-Api GET "/campuses" $adminToken
    $testCampus = $campusList.data | Where-Object { $_.campusName -eq "Smoke Campus" } | Select-Object -First 1

    $venues = Invoke-Api GET "/venues" $studentToken
    Assert-Code $venues "GET /venues"

    Assert-Code (Invoke-Api POST "/venues" $adminToken @{ venueName = "Smoke Building"; roomNumber = "T101"; capacity = 2; campusId = $testCampus.id }) "POST /venues"
    $venueList = Invoke-Api GET "/venues?campusId=$($testCampus.id)" $adminToken
    $testVenue = $venueList.data | Where-Object { $_.venueName -eq "Smoke Building" } | Select-Object -First 1

    $categories = Invoke-Api GET "/categories" $studentToken
    Assert-Code $categories "GET /categories"

    Assert-Code (Invoke-Api POST "/categories" $adminToken @{ categoryName = "Smoke Category" }) "POST /categories"
    $categoryList = Invoke-Api GET "/categories" $adminToken
    $testCategory = $categoryList.data | Where-Object { $_.categoryName -eq "Smoke Category" } | Select-Object -First 1

    $activities = Invoke-Api GET "/activities?page=1&size=10" $studentToken
    Assert-Code $activities "GET /activities"
    $seedActivityId = $activities.data.list[0].id

    Assert-Code (Invoke-Api GET "/activities/$seedActivityId" $studentToken) "GET /activities/{id}"

    $draft = Invoke-Api POST "/activities" $organizerToken @{
        title = "Smoke Test Activity"
        venueId = $testVenue.id
        categoryId = $testCategory.id
        startTime = "2026-06-20T14:00:00"
        endTime = "2026-06-20T16:00:00"
        enrollDeadline = "2026-06-19T22:00:00"
        capacityLimit = 2
        posterUrl = ""
        description = "Created by smoke test"
    }
    Assert-Code $draft "POST /activities"
    $activityId = $draft.data.id

    Assert-Code (Invoke-Api PUT "/activities/$activityId" $organizerToken @{
        title = "Smoke Test Activity Updated"
        venueId = $testVenue.id
        categoryId = $testCategory.id
        startTime = "2026-06-20T14:00:00"
        endTime = "2026-06-20T16:00:00"
        enrollDeadline = "2026-06-19T22:00:00"
        capacityLimit = 2
        posterUrl = ""
        description = "Updated by smoke test"
    }) "PUT /activities/{id}"

    Assert-Code (Invoke-Api POST "/activities/$activityId/submit" $organizerToken) "POST /activities/{id}/submit"
    Assert-Code (Invoke-Api GET "/activities?status=PENDING_REVIEW" $adminToken) "GET /activities status=PENDING_REVIEW"
    Assert-Code (Invoke-Api POST "/activities/$activityId/review" $adminToken @{ result = "APPROVED"; reason = "" }) "POST /activities/{id}/review"

    $enrollForCancel = Invoke-Api POST "/activities/$seedActivityId/registrations" $studentToken
    Assert-Code $enrollForCancel "POST /activities/{id}/registrations seed"
    Assert-Code (Invoke-Api DELETE "/registrations/$($enrollForCancel.data.registrationId)" $studentToken) "DELETE /registrations/{id}"

    $enrollForCheckIn = Invoke-Api POST "/activities/$activityId/registrations" $studentToken
    Assert-Code $enrollForCheckIn "POST /activities/{id}/registrations approved"
    $registrationId = $enrollForCheckIn.data.registrationId

    Assert-Code (Invoke-Api GET "/registrations/my" $studentToken) "GET /registrations/my"
    Assert-Code (Invoke-Api GET "/activities/$activityId/registrations" $organizerToken) "GET /activities/{id}/registrations"

    $code = Invoke-Api GET "/registrations/$registrationId/check-in-code" $studentToken
    Assert-Code $code "GET /registrations/{id}/check-in-code"
    Assert-Code (Invoke-Api PATCH "/registrations/check-in" $organizerToken @{ checkInCode = $code.data.checkInCode }) "PATCH /registrations/check-in"

    Assert-Code (Invoke-Api GET "/stats/overview" $adminToken) "GET /stats/overview"
    Assert-Code (Invoke-Api GET "/stats/campus-usage" $adminToken) "GET /stats/campus-usage"
    Assert-Code (Invoke-Api GET "/stats/category-popularity" $adminToken) "GET /stats/category-popularity"

    Assert-Code (Invoke-Api POST "/activities/$activityId/cancel" $organizerToken @{ reason = "smoke test cleanup" }) "POST /activities/{id}/cancel"

    Write-Host "ALL API SMOKE TESTS PASSED"
} finally {
    Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
}
