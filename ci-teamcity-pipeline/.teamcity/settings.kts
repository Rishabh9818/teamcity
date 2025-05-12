object GitLeaksScanPipeline : BuildType({
    name = "GitLeaks Security Scan"

    vcs {
        root(DslContext.settingsRoot)
    }

    params {
        param("repo.url", "https://github.com/your/repo.git")  // Git repo URL
        param("config.path", "%teamcity.build.checkoutDir%/ci-teamcity-pipeline/gitleaks.toml")  // Path to config
        param("report.path", "%teamcity.build.checkoutDir%/gitleaks-report.json")  // Path to output report
        param("verbose", "true")  // Optionally enable verbose output
    }

    steps {
        script {
            name = "Run GitLeaks Security Scan"
            scriptContent = """
                echo "Starting GitLeaks Security Scan..."
                cd %teamcity.build.checkoutDir%/ci-teamcity-pipeline
                
                # Run the gitleaksWrapper.groovy script
                groovy gitleaksWrapper.groovy "%repo.url%" "%config.path%" "%report.path%" "%verbose%"
                
                # Check the result of the scan
                if [ $? -ne 0 ]; then
                    echo "GitLeaks scan failed. Check the report for details."
                    exit 1
                else
                    echo "GitLeaks scan completed successfully."
                fi
            """.trimIndent()
        }
    }

    // Publish the gitleaks report as an artifact
    artifactRules = "gitleaks-report.json => security-reports"
})
