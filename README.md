# LyticalCardScanner SDK

LyticalCardScanner is a lightweight, easy-to-use SDK for integrating card scanning capabilities into your Android application. This document provides instructions on how to include LyticalCardScanner in your project.

## Getting Started

To integrate LyticalCardScanner into your Android project, follow the steps below.

### Step 1: Add JitPack Repository

First, add the JitPack repository to your project's build configuration to enable downloading the SDK from JitPack. 

If you are using the Gradle version 7 or newer (which includes the `settings.gradle` file), add the following to your `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

For older Gradle versions, add the JitPack repository to your root build.gradle inside the allprojects section:

```
allprojects {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2: Add SDK Dependency
Next, add the LyticalCardScanner SDK as a dependency in your app-level build.gradle file:

```
dependencies {
    implementation 'com.github.NightingaleNath:LyticalCardScanner:0.0.1'
}
```

Step 3: Sync Your Project
After adding the repository and dependency, sync your project with Gradle files to download the SDK.

Usage
To use the LyticalCardScanner SDK in your application, refer to the following basic example or visit our documentation for detailed usage instructions.

```
@androidx.camera.core.ExperimentalGetImage
class MainActivity : AppCompatActivity(), SdkResultCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SdkActivityLauncher.setCallback(this)

        binding.scanButton.setOnClickListener {
            SdkActivityLauncher.launchActivity(this, SdkActivityType.CREDIT_CARD_SCANNER)
        }

        binding.nfcButton.setOnClickListener {
            SdkActivityLauncher.launchActivity(this, SdkActivityType.NFC_READER)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        SdkActivityLauncher.setCallback(null)
    }

    override fun onSdkResult(result: Bundle) {
        val cardNumber = result.getString("CARD_NUMBER", "")
        val expiryDate = result.getString("EXPIRY_DATE", "")
        val cardType = result.getString("CARD_TYPE", "")
        if (!cardNumber.isNullOrEmpty() && !expiryDate.isNullOrEmpty()) {
            Toast.makeText(this, "Card Number: $cardNumber, Expiry Date: $expiryDate", Toast.LENGTH_LONG).show()
        }
    }
}
```

Support
For issues, feature requests, or additional help, please open an issue on our GitHub repository.

Thank you for choosing LyticalCardScanner for your card scanning needs!


### Additional Notes:

- **Documentation Link**: Replace the placeholder `(#[documentation])` with the actual link to your SDK's documentation if available.
- **Example Usage**: Providing a simple usage example directly in the README can greatly help developers get started. Consider adding a basic example that shows how to initiate a scan or integrate your SDK into an activity.
- **Support Section**: Adjust the support section based on how you prefer to handle queries. If you use GitHub issues, the current text is suitable. Otherwise, provide an email or a link to a support forum.

This template should give you a good starting point for creating a README for your SDK. Adjust it as necessary to match your project's requirements and to provide all the necessary information to your SDK users.

