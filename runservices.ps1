$root = "D:\Downloads\gdb-service-javafullstack-updated-fixs-crs\"

$services = @(
    "aadhar-service",
    "account-service",
    "auth-service",
    "company-service",
    "payment-gateway-service",
    "transactions-service",
    "users-service"
)

$first = $true

foreach ($service in $services) {

    $servicePath = Join-Path $root $service

    if ($first) {
        wt -d $servicePath powershell -NoExit -Command "mvn spring-boot:run"
        $first = $false
    }
    else {
        wt -w 0 new-tab -d $servicePath powershell -NoExit -Command "mvn spring-boot:run"
    }

    Start-Sleep -Seconds 2
}