def notifier = new me.digi.Slack(this);

node('android') {
    try {
        stage('clean-workspace') {
            step([$class: 'WsCleanup'])
        }
        stage('checkout') {
            checkout(scm)
        }
        stage('build') {
        //build all of modules here
            sh "./gradlew assemble -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        stage('lint') {
            //run static only on release build type
            sh "./gradlew lintRelease -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        stage('test') {
            sh "./gradlew test -PBUILD_NUMBER=${env.BUILD_NUMBER}"
        }
        //Disable instrumented tests for now
        //stage('android-test') {
        //    lock(resource: "emulator_${env.NODE_NAME}") {
        //        sh "./gradlew connectedAndroidTest"
        //    }
        //}
        stage('publish-junit') {
            junit '**/TEST-*.xml'
        }
        stage('artifacts') {
            if (env.BRANCH_NAME == "master") {
                dir('sdk/build/outputs/aar') {
                    archiveArtifacts artifacts: '*.aar', fingerprint: true;
                }
            }
        }
        stage('notify') {
            notifier.success()
        }
    } catch (err) {
        notifier.fail()
        currentBuild.result = "FAILURE"
        echo err.message
    }
}

