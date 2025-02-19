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
    implementation 'com.github.NightingaleNath:LyticalCardScanner:1.0.1
}
```

Step 2A: Target and Compile SDK:
Ensure your sdk levels for both target and compile is 35 or above:

```
compileSdk 35
targetSdk 35
ndkVersion = "27.0.12077973"

```

```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
kotlinOptions {
    jvmTarget = '17'
}
```

Your project level build.gradle should be from:

```
plugins {
    id 'com.android.application' version '8.7.3' apply false
    id 'org.jetbrains.kotlin.android' version '2.1.10' apply false
    id 'com.android.library' version '8.7.3' apply false
}

```

Set the gradle-wrapper.properties to 8.9 or higher:

```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip

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
        
        if (!SdkActivityLauncher.isNfcSupported(this)) {
            AlertDialog.Builder(this)
                .setTitle("NFC Not Supported")
                .setMessage("Your device does not support NFC. You cannot use NFC-based features.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.scanButton.setOnClickListener {
            SdkActivityLauncher.launchActivity(this, SdkActivityType.CREDIT_CARD_SCANNER, "")
        }

        binding.nfcButton.setOnClickListener {
            val totalAmount = "10.00"

            navigateToNFC(totalAmount)
        }
    }
    
    private fun navigateToNFC(totalAmount: String) {
        ViewToggleHelper.showIsDone = false
        SdkActivityLauncher.launchActivity(this, SdkActivityType.NFC_READER, amount = totalAmount)
    }

    override fun onDestroy() {
        super.onDestroy()

        SdkActivityLauncher.setCallback(null)
    }

    override fun onSdkResult(result: Bundle) {
        val cardNumber = result.getString("CARD_NUMBER", "")
        val expiryDate = result.getString("EXPIRY_DATE", "")
//        val cardType = result.getString("CARD_TYPE", "")
//        showBoolean = result.getBoolean("CAN_EDIT", false)
        if (!cardNumber.isNullOrEmpty() && !expiryDate.isNullOrEmpty()) {
            Toast.makeText(this, "Number: $cardNumber, Date: $expiryDate", Toast.LENGTH_LONG).show()
        }
    }
}
```

Usage for flutter Native Channel
To use the LyticalCardScanner SDK in your flutter android application, refer to the following basic example or visit our documentation for detailed usage instructions.

```
@androidx.camera.core.ExperimentalGetImage
class MainActivity: FlutterActivity(), SdkResultCallback {

	private val CHANNEL = "nfc_channel"
	private var nfcResult: MethodChannel.Result? = null

	override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
		super.configureFlutterEngine(flutterEngine)

		SdkActivityLauncher.setCallback(this)

		MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
			when (call.method) {
				"isNfcSupported" -> {
					val isSupported = SdkActivityLauncher.isNfcSupported(this)
					result.success(isSupported)
				}
				"startNfcReader" -> {
					val amount = call.argument<String>("amount") ?: "0.00"
					startNfcReader(amount, result)
				}
				else -> {
					result.notImplemented()
				}
			}
		}
	}

	private fun startNfcReader(amount: String, result: MethodChannel.Result) {
		if (!SdkActivityLauncher.isNfcSupported(this)) {
			result.error("NFC_NOT_SUPPORTED", "NFC is not supported on this device.", null)
			return
		}
		nfcResult = result
		ViewToggleHelper.showIsDone = false
		SdkActivityLauncher.launchActivity(this, SdkActivityType.NFC_READER, amount = amount)
	}

	override fun onSdkResult(result: Bundle) {
		val cardNumber = result.getString("CARD_NUMBER", "")
		val expiryDate = result.getString("EXPIRY_DATE", "")

		Log.d("NFC_FLUTTER", "onSdkResult received - CardNumber: $cardNumber, ExpiryDate: $expiryDate")

		if (!cardNumber.isNullOrEmpty() && !expiryDate.isNullOrEmpty()) {
			Log.d("NFC_FLUTTER", "Successfully read NFC card data.")
			nfcResult?.success(mapOf("cardNumber" to cardNumber, "expiryDate" to expiryDate))
			Log.d("NFC_FLUTTER", "Success result sent to Flutter")
		} else {
			Log.e("NFC_FLUTTER", "NFC reading failed: Empty cardNumber or expiryDate")
			nfcResult?.error("NFC_READ_ERROR", "Failed to read card data.", null)
		}
		nfcResult = null
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d("NFC_FLUTTER", "MainActivity destroyed. Resetting callback.")
		SdkActivityLauncher.setCallback(null)
	}
}

```

BONUS POINT:
To use it in your flutter code to use the native channel call, you can create a file in your flutter project and use it as below:

```
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class NfcService {
  static const MethodChannel _channel = MethodChannel('nfc_channel');

  static Future<bool> isNfcSupported() async {
    try {
      final bool isSupported = await _channel.invokeMethod('isNfcSupported');
      return isSupported;
    } catch (e) {
      return false;
    }
  }

  static Future<Map<String, dynamic>?> startNfcReader(String amount) async {
    try {
      final result =
          await _channel.invokeMethod('startNfcReader', {"amount": amount});

      debugPrint('Raw NFC result received: $result');

      if (result != null && result is Map) {
        final processedResult = Map<String, dynamic>.from(result);
        debugPrint('Processed NFC result: $processedResult');
        return {
          "cardNumber": processedResult["cardNumber"] ?? "",
          "expiryDate": processedResult["expiryDate"] ?? ""
        };
      } else {
        debugPrint('NFC result was null or not a Map');
        return null;
      }
    } catch (e) {
      debugPrint('Error during NFC reading: $e');
      return null;
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

