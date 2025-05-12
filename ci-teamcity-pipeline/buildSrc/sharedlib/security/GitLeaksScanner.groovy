package wrapper

class GitLeaksScanner {
    static boolean scan(String repoPath, String configPath, String reportPath, String scanOptions) {

        def command = [
            'gitleaks',
            'detect',
            "--path=${repoPath}",
            "--config=${configPath}",
            "--report=${reportPath}"
        ]
        if (scanOptions) {
            command += scanOptions.split(' ')
        }

        println "[DEBUG] Executing: ${command.join(' ')}"

        try {
            def process = command.execute()
            def stdout = new StringBuffer()
            def stderr = new StringBuffer()
            process.waitForProcessOutput(stdout, stderr)

            println "[OUTPUT]\n${stdout}"
            if (stderr) println "[ERROR]\n${stderr}"

            return process.exitValue() == 0
        } catch (IOException e) {
            println "[ERROR] Failed to execute gitleaks: ${e.message}"
            return false
        }
    }
}


// -----------------

// package sharedlib.security

// class GitLeaksScanner {
//     /**
//      * Run a GitLeaks scan on the repository
//      * @param repoPath Path to the repository to scan
//      * @param reportPath Path where the JSON report will be saved
//      * @param options Additional command-line options for gitleaks
//      * @return true if scan passes with no leaks found, false if leaks are found
//      */
//     static boolean scan(String repoPath = null, String reportPath = "./gitleaks-report.json", Map options = [:]) {
//         def repoMessage = repoPath ? "repository at ${repoPath}" : "current directory"
//         println "Running GitLeaks security scan on ${repoMessage}, report will be saved to: ${reportPath}"
        
//         // Build the gitleaks command with default options
//         def cmd = ["gitleaks", "detect", "--report-path=${reportPath}", "--report-format=json"]
        
//         // If a specific repo path is provided, add it to the command
//         if (repoPath) {
//             cmd.add("--source=${repoPath}")
//         }
        
//         // Add any custom options
//         options.each { key, value ->
//             cmd.add("--${key}=${value}")
//         }
        
//         // Execute the gitleaks command
//         def process = new ProcessBuilder(cmd).redirectErrorStream(true).start()
//         process.inputStream.eachLine { line ->
//             println line
//         }
//         process.waitFor()
        
//         // Exit code 1 means leaks were found
//         def exitCode = process.exitValue()
//         if (exitCode == 1) {
//             println "GitLeaks found security vulnerabilities. Check the report for details."
//             return false
//         } else if (exitCode != 0) {
//             throw new RuntimeException("GitLeaks scan failed with exit code ${exitCode}")
//         }
        
//         println "GitLeaks scan completed successfully with no security issues found."
//         return true
//     }
    
//     /**
//      * Helper method to configure a custom gitleaks scan with specific settings
//      */
//     static boolean customScan(Map config) {
//         def repoPath = config.repoPath
//         def reportPath = config.reportPath ?: "./gitleaks-report.json"
//         def options = config.options ?: [:]
        
//         // Add optional parameters if provided
//         if (config.configPath) {
//             options.put("config", config.configPath)
//         }
        
//         if (config.depth) {
//             options.put("depth", config.depth)
//         }
        
//         if (config.verbose) {
//             options.put("verbose", "")
//         }
        
//         return scan(repoPath, reportPath, options)
//     }
// }