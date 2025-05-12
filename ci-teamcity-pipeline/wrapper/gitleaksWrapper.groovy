#!/usr/bin/env groovy

class GitLeaksScanner {
    static boolean scan(String repoUrl, String configPath, String reportPath, boolean verbose) {
        def cloneDir = "./cloned-repo"
        println "[INFO] Cloning repo: ${repoUrl} to ${cloneDir}"

        def cloneCommand = ["git", "clone", "--depth", "1", repoUrl, cloneDir]
        println "[DEBUG] Executing clone: ${cloneCommand.join(' ')}"
        def cloneProcess = cloneCommand.execute()
        def cloneOut = new StringBuffer()
        def cloneErr = new StringBuffer()
        cloneProcess.waitForProcessOutput(cloneOut, cloneErr)

        if (cloneProcess.exitValue() != 0) {
            println "[ERROR] Git clone failed:\n${cloneErr}"
            return false
        }

        println "[INFO] Clone successful. Starting GitLeaks scan..."

        def command = [
            "gitleaks", "detect",
            "--path=${cloneDir}",
            "--report=${reportPath}",  // Correctly specifying the report file path
            "--format=json",
            "--no-git",
            "--debug"
        ]
        
        if (verbose) {
            command << "--verbose"  // Separate verbose flag
        }
        if (configPath) {
            command << "--config-path=${configPath}"
        }

        println "[DEBUG] Executing: ${command.join(' ')}"

        try {
            def process = command.execute()
            def stdout = new StringBuffer()
            def stderr = new StringBuffer()
            process.waitForProcessOutput(stdout, stderr)

            println "[OUTPUT]\n${stdout}"
            if (stderr) println "[ERROR]\n${stderr}"

            if (stdout.toString().contains("Usage:") && stdout.toString().contains("gitleaks")) {
                println "[ERROR] Invalid flag usage. Check CLI arguments."
                return false
            }

def reportFile = new File(reportPath)
if (reportFile.exists() && reportFile.text.trim().startsWith("[")) {
    println "[WARNING] GitLeaks scan found issues. Check report at ${reportPath}."
    return false
} else {
    println "[SUCCESS] GitLeaks scan completed with no leaks."
    return true
}
        } catch (IOException e) {
            println "[ERROR] Exception occurred during execution: ${e.message}"
            return false
        }
    }
}

// --- CLI Entry Point ---

if (this.args.size() < 3 || this.args.size() > 4) {
    println "Usage: groovy gitleaksWrapper.groovy <git-url> <config-path> <report-path> [--verbose]"
    System.exit(1)
}

def gitUrl = this.args[0]
def configPath = this.args[1] != "null" ? this.args[1] : null
def reportPath = this.args[2]
def verboseFlag = (this.args.size() == 4 && this.args[3] == "--verbose")

def result = GitLeaksScanner.scan(gitUrl, configPath, reportPath, verboseFlag)
System.exit(result ? 0 : 1)


// --- CLI Entry Point ---

// if (this.args.size() < 3 || this.args.size() > 4) {
//     println "Usage: groovy gitleaksWrapper.groovy <git-url> <config-path> <report-path> [--verbose]"
//     System.exit(1)
// }

// def gitUrl = this.args[0]  // Git URL from TeamCity param
// def configPath = this.args[1]  // Config path from TeamCity param
// def reportPath = this.args[2]  // Report path from TeamCity param
// def verboseFlag = (this.args.size() == 4 && this.args[3] == "--verbose")

// def result = GitLeaksScanner.scan(gitUrl, configPath, reportPath, verboseFlag)
// System.exit(result ? 0 : 1)

// if (this.args.size() < 3 || this.args.size() > 4) {
//     println "Usage: groovy gitleaksWrapper.groovy <git-url> <config-path> <report-path> [--verbose]"
//     System.exit(1)
// }

// def gitUrl = this.args[0]
// def configPath = this.args[1]
// def reportPath = this.args[2]
// def verboseFlag = (this.args.size() == 4 && this.args[3] == "--verbose")

// def result = GitLeaksScanner.scan(gitUrl, configPath, reportPath, verboseFlag)
// System.exit(result ? 0 : 1)


// works^

// ------------------
// package wrapper

// class GitLeaksScanner {
//     static boolean scan(String gitUrl, String configPath, String reportPath, String scanOptions) {
//         def cloneDir = "./cloned-repo"
//         println "[INFO] Cloning repo: ${gitUrl} to ${cloneDir}"

//         def cloneCommand = ["git", "clone", "--depth", "1", gitUrl, cloneDir]
//         println "[DEBUG] Executing clone: ${cloneCommand.join(' ')}"
//         def cloneProcess = cloneCommand.execute()
//         def cloneOut = new StringBuffer()
//         def cloneErr = new StringBuffer()
//         cloneProcess.waitForProcessOutput(cloneOut, cloneErr)

//         if (cloneProcess.exitValue() != 0) {
//             println "[ERROR] Git clone failed:\n${cloneErr}"
//             return false
//         }

//         println "[INFO] Clone successful. Starting GitLeaks scan..."

//         def command = [
//             'gitleaks',
//             'detect',
//             "--source=${cloneDir}",
//             "--config-path=${configPath}",
//             "--report-path=${reportPath}",
//             "--report-format=json"
//         ]
//         if (scanOptions) {
//             command += scanOptions.tokenize(' ')
//         }

//         println "[DEBUG] Executing: ${command.join(' ')}"

//         try {
//             def process = command.execute()
//             def stdout = new StringBuffer()
//             def stderr = new StringBuffer()
//             process.waitForProcessOutput(stdout, stderr)

//             println "[OUTPUT]\n${stdout}"
//             if (stderr) println "[ERROR]\n${stderr}"

//             def exitCode = process.exitValue()
//             if (exitCode == 0) {
//                 println "[SUCCESS] No leaks found."
//                 return true
//             } else {
//                 println "[WARNING] Leaks found or scan failed. Check report."
//                 return false
//             }
//         } catch (Exception e) {
//             println "[ERROR] Exception occurred: ${e.message}"
//             return false
//         }
//     }
// }

// if (this.args.size() < 3) {
//     println "Usage: groovy gitleaksWrapper.groovy <git-url> <config-path> <report-path> [extra-options]"
//     System.exit(1)
// }

// def gitUrl = this.args[0]
// def configPath = this.args[1]
// def reportPath = this.args[2]
// def options = this.args.size() > 3 ? this.args[3..-1].join(' ') : ""

// def result = GitLeaksScanner.scan(gitUrl, configPath, reportPath, options)
// System.exit(result ? 0 : 1)

// --------------------works2----------------------
// package wrapper

// class GitLeaksScanner {
//     /**
//      * Run GitLeaks scan with modern CLI flags (compatible with v8+)
//      * @param repoPath Path to the repo or directory to scan
//      * @param configPath Path to the gitleaks config TOML file
//      * @param reportPath Path to save the scan report
//      * @param scanOptions Optional additional flags, space-separated
//      * @return true if no leaks found, false otherwise
//      */
//     static boolean scan(String repoPath, String configPath, String reportPath, String scanOptions) {
//         def command = [
//             'gitleaks',
//             'detect',
//             "--path=${repoPath}",
//             "--config-path=${configPath}",
//             "--report=${reportPath}",
//             "--format=json"
//         ]

//         if (scanOptions) {
//             command += scanOptions.tokenize(' ')
//         }

//         println "[DEBUG] Executing: ${command.join(' ')}"

//         try {
//             def process = command.execute()
//             def stdout = new StringBuffer()
//             def stderr = new StringBuffer()
//             process.waitForProcessOutput(stdout, stderr)

//             println "[OUTPUT]\n${stdout}"
//             if (stderr) println "[ERROR]\n${stderr}"

//             // If usage/help text appears, it's likely a flag issue
//             if (stdout.toString().contains("Usage:") && stdout.toString().contains("gitleaks")) {
//                 println "[ERROR] Detected usage help output. Likely due to incorrect flags or CLI mismatch."
//                 return false
//             }

//             def exitCode = process.exitValue()
//             if (exitCode == 0) {
//                 println "[SUCCESS] GitLeaks scan completed with no leaks."
//                 // If no leaks found, write an empty JSON array to the report file
//                 if (stdout.toString().contains("No leaks found")) {
//                     println "[INFO] No leaks found, creating an empty JSON report."
//                     new File(reportPath).text = '[]'  // Write an empty JSON array to the report
//                 }
//                 return true
//             } else if (exitCode == 1) {
//                 println "[WARNING] GitLeaks scan found issues. Check the report at ${reportPath}."
//                 return false
//             } else {
//                 println "[ERROR] GitLeaks scan failed with exit code ${exitCode}."
//                 return false
//             }
//         } catch (IOException e) {
//             println "[ERROR] Failed to execute gitleaks: ${e.message}"
//             return false
//         }
//     }
// }

// // Run from CLI: groovy wrapper/gitleaksWrapper.groovy <repoPath> <configPath> <reportPath> [extraOptions]
// if (this.args.size() < 3) {
//     println "Usage: groovy gitleaksWrapper.groovy <repoPath> <configPath> <reportPath> [extraOptions]"
//     System.exit(1)
// }

// def repoPath = this.args[0]
// def configPath = this.args[1]
// def reportPath = this.args[2]
// def options = this.args.size() > 3 ? this.args[3..-1].join(' ') : ""

// def result = GitLeaksScanner.scan(repoPath, configPath, reportPath, options)
// System.exit(result ? 0 : 1)



// -------------------works--------------------
// #!/usr/bin/env groovy

// class GitLeaksScanner {
//     static boolean scan(String repoPath, String configPath, String reportPath, String scanOptions) {
//         def command = [
//             'gitleaks',
//             'detect',
//             "--source=${repoPath}",
//             "--config=${configPath}",
//             "--report-path=${reportPath}"
//         ]
//         if (scanOptions) {
//             command += scanOptions.split(' ')
//         }

//         println "[DEBUG] Executing: ${command.join(' ')}"

//         try {
//             def process = command.execute()
//             def stdout = new StringBuffer()
//             def stderr = new StringBuffer()
//             process.waitForProcessOutput(stdout, stderr)

//             println "[OUTPUT]\n${stdout}"
//             if (stderr) println "[ERROR]\n${stderr}"

//             return process.exitValue() == 0
//         } catch (IOException e) {
//             println "[ERROR] Failed to execute gitleaks: ${e.message}"
//             return false
//         }
//     }
// }

// // ---- Main execution block ----

// if (this.args.length < 3) {
//     println "Usage: groovy gitleaksWrapper.groovy <repoPath> <configPath> <reportPath> [-v or other options]"
//     System.exit(1)
// }

// def repoPath = args[0]
// def configPath = args[1]
// def reportPath = args[2]
// def extraOptions = args.length > 3 ? args[3..-1].join(" ") : ""

// def success = GitLeaksScanner.scan(repoPath, configPath, reportPath, extraOptions)
// if (success) {
//     println "[SUCCESS] GitLeaks scan completed with no leaks."
//     System.exit(0)
// } else {
//     println "[FAILURE] GitLeaks scan found issues or failed to run."
//     System.exit(1)
// }


// --------------------

// #!/usr/bin/env groovy

// import wrapper.GitLeaksScanner

// def main(String[] args) {
//     println "[INFO] Starting GitLeaks security scan wrapper..."

//     if (args.length < 3) {
//         println "[ERROR] Usage: groovy gitleaksWrapper.groovy <repoPath> <configPath> <reportPath> [options]"
//         System.exit(1)
//     }

//     def repoPath = args[0]
//     def configPath = args[1]
//     def reportPath = args[2]
//     def scanOptions = args.length > 3 ? args[3..-1].join(' ') : ''

//     println "[DEBUG] Repo Path: ${repoPath}"
//     println "[DEBUG] Config Path: ${configPath}"
//     println "[DEBUG] Report Path: ${reportPath}"
//     println "[DEBUG] Additional Options: ${scanOptions}"

//     try {
//         println "[INFO] Running gitleaks scan..."
//         def result = GitLeaksScanner.scan(repoPath, configPath, reportPath, scanOptions)
//         println "[INFO] GitLeaks scan finished with status: ${result ? 'Success' : 'Failure'}"
//     } catch (Exception e) {
//         println "[ERROR] Exception during scan: ${e.message}"
//         e.printStackTrace()
//         System.exit(1)
//     }
// }

// main(this.args)


// --------------

// #!/usr/bin/env groovy

// @Grab(group='org.codehaus.groovy', module='groovy-all', version='2.5.14')

// import sharedlib.security.GitLeaksScanner

// static void main(String[] args) {
//     println "Starting GitLeaks security scan wrapper..."

//     def repoPath = args.length > 0 ? args[0] : System.getProperty("repo.path", "~/ops/abc")
//     def configPath = args.length > 1 ? args[1] : System.getProperty("config.path", "")
//     def reportPath = args.length > 2 ? args[2] : System.getProperty("report.path", "./gitleaks-report.json")

//     // Expand `~` to $HOME
//     if (repoPath.startsWith("~")) repoPath = repoPath.replaceFirst("~", System.getProperty("user.home"))
//     if (configPath.startsWith("~")) configPath = configPath.replaceFirst("~", System.getProperty("user.home"))

//     def repoDir = new File(repoPath)
//     if (!repoDir.exists()) {
//         println "[ERROR] Repository path does not exist: ${repoPath}"
//         System.exit(1)
//     }

//     println "[INFO] Scanning repository at: ${repoPath}"

//     def scanOptions = [:]
//     if (configPath) scanOptions.put("config", configPath)

//     def result = GitLeaksScanner.scan(repoPath, reportPath, scanOptions)

//     println "[Wrapper] GitLeaks scan complete. Status: ${result ? 'PASSED' : 'FAILED'}"
//     System.exit(result ? 0 : 1)
// }


// -------------

// #!/usr/bin/env groovy

// @GrabResolver(name='groovy', root='file:///' + new File('.').absolutePath)
// @Grab(group='org.codehaus.groovy', module='groovy-all', version='2.5.14')

// println "Starting GitLeaks security scan wrapper..."

// import sharedlib.security.GitLeaksScanner

// // Resolve project structure
// def rootDir = new File(getClass().protectionDomain.codeSource.location.path).parentFile
// def sharedlibDir = new File(rootDir, "../buildSrc/sharedlib")
// def classLoader = this.class.classLoader

// // Dynamically add sharedlib to classpath
// if (classLoader.metaClass.respondsTo(classLoader, "addURL", URL)) {
//     classLoader.addURL(sharedlibDir.toURI().toURL())
// } else if (classLoader.hasProperty("rootLoader")) {
//     classLoader.rootLoader.addURL(sharedlibDir.toURI().toURL())
// } else {
//     println "Unable to add sharedlib directory to classpath."
// }

// // Resolve args or use fallback system properties
// def repoPath = args.length > 0 ? args[0] : System.getProperty("repo.path", "~/ops/abc")
// def configPath = args.length > 1 ? args[1] : System.getProperty("config.path", "")
// def reportPath = args.length > 2 ? args[2] : System.getProperty("report.path", "./gitleaks-report.json")

// // Expand `~` to home
// if (repoPath.startsWith("~")) repoPath = repoPath.replaceFirst("~", System.getProperty("user.home"))
// if (configPath.startsWith("~")) configPath = configPath.replaceFirst("~", System.getProperty("user.home"))

// // Validate repo path
// def repoDir = new File(repoPath)
// if (!repoDir.exists()) {
//     println "[ERROR] Repository path does not exist: ${repoPath}"
//     System.exit(1)
// }

// println "[INFO] Scanning repository at: ${repoPath}"

// def scanOptions = [:]
// if (configPath) scanOptions.put("config", configPath)

// def result = GitLeaksScanner.scan(repoPath, reportPath, scanOptions)

// println "[Wrapper] GitLeaks scan complete. Status: ${result ? 'PASSED' : 'FAILED'}"
// System.exit(result ? 0 : 1)
// -------------------------------------------1------

// #!/usr/bin/env groovy

// // Add the directory containing the sharedlib package to the classpath
// // This assumes this script is in the 'wrapper' directory, which is a sibling to 'sharedlib'
// def rootDir = new File(getClass().protectionDomain.codeSource.location.path).parent
// def sharedlibDir = new File(rootDir).parent
// // Add the parent directory to the classpath so that "sharedlib" can be found
// this.class.classLoader.rootLoader.addURL(new File(sharedlibDir).toURI().toURL())

// println "Starting GitLeaks security scan wrapper..."

// // Import the GitLeaksScanner class
// import sharedlib.security.GitLeaksScanner

// println "[Wrapper] Starting GitLeaks scan via GitLeaksScanner..."

// // Get repository path from arguments or system properties
// def repoPath = args.length > 0 ? args[0] : System.getProperty("repo.path", "~/ops/abc")

// // Expand home directory if needed
// if (repoPath.startsWith("~")) {
//     repoPath = repoPath.replace("~", System.getProperty("user.home"))
// }

// // Ensure the repository exists
// def repoDir = new File(repoPath)
// if (!repoDir.exists()) {
//     println "ERROR: Repository path does not exist: ${repoPath}"
//     System.exit(1)
// }

// println "Scanning repository at: ${repoPath}"

// // Get config path from arguments or system properties
// def configPath = args.length > 1 ? args[1] : System.getProperty("config.path", "")
// def reportPath = args.length > 2 ? args[2] : System.getProperty("report.path", "./gitleaks-report.json")

// def scanOptions = [:]
// if (configPath) {
//     scanOptions.put("config", configPath)
// }

// // Call the scan method with the specific repository path
// def scanResult = GitLeaksScanner.scan(repoPath, reportPath, scanOptions)

// println "[Wrapper] GitLeaks scan complete. Status: ${scanResult ? 'PASSED' : 'FAILED'}"

// // Exit with appropriate status code
// System.exit(scanResult ? 0 : 1)