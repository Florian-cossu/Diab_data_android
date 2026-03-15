import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

val scriptDir = File(System.getProperty("user.dir"))
println("📂 Working directory: $scriptDir")

val today = LocalDate.now()
val dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun dateTime(date: LocalDate, hour: Int, minute: Int): String =
    date.atTime(hour, minute, 0).format(dtFormatter)

val json = """
{
  "appointments": [
    {
      "createdAt": "${today.minusMonths(2)}",
      "date": "${dateTime(today.plusDays(3), 10, 30)}",
      "doctor": "Dr. Eric M. Ellison",
      "id": 0,
      "isArchived": false,
      "notes": "Bilan trimestriel",
      "type": "appointment",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(2)}",
      "date": "${dateTime(today.plusDays(45), 14, 0)}",
      "doctor": "Pr. Douglas T. Cobos",
      "id": 0,
      "isArchived": false,
      "notes": "Bilan annuel avec analyses complètes",
      "type": "annual_checkup",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(1)}",
      "date": "${dateTime(today.plusDays(90), 9, 15)}",
      "doctor": "Dr. Brian Leclair",
      "id": 0,
      "isArchived": false,
      "notes": "Fond d'oeil",
      "type": "appointment",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(6)}",
      "date": "${dateTime(today.minusDays(30), 11, 0)}",
      "doctor": "Dr. Michele M. Pankey",
      "id": 0,
      "isArchived": true,
      "notes": "Contrôle rénal annuel",
      "type": "appointment",
      "updatedAt": "${today.minusDays(30)}"
    }
  ],
  "devices": [
    {
      "batchNumber": "DBOENE58",
      "createdAt": "${today.minusDays(1)}",
      "date": "${today.minusDays(1)}",
      "deviceType": "WIRELESS_PATCH",
      "id": 0,
      "isArchived": false,
      "isFaulty": false,
      "isLifeSpanOver": false,
      "isReported": false,
      "lifeSpan": 3,
      "lifeSpanEndDate": "${today.plusDays(2)}",
      "manufacturer": "Insulet",
      "name": "Omnipod pod",
      "referenceNumber": "",
      "serialNumber": "VEODJE5937EBBODLDB",
      "updatedAt": "$today"
    },
    {
      "batchNumber": "BEIEJ68",
      "createdAt": "${today.minusDays(5)}",
      "date": "${today.minusDays(5)}",
      "deviceType": "CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER",
      "id": 0,
      "isArchived": false,
      "isFaulty": false,
      "isLifeSpanOver": false,
      "isReported": false,
      "lifeSpan": 90,
      "lifeSpanEndDate": "${today.plusDays(85)}",
      "manufacturer": "Dexcom",
      "name": "Dexcom G7 Transmitter",
      "referenceNumber": "REF-DX7-2024",
      "serialNumber": "BEIFB69-DKEODB",
      "updatedAt": "$today"
    },
    {
      "batchNumber": "HEOEB79",
      "createdAt": "${today.minusDays(3)}",
      "date": "${today.minusDays(3)}",
      "deviceType": "CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR",
      "id": 0,
      "isArchived": false,
      "isFaulty": false,
      "isLifeSpanOver": false,
      "isReported": false,
      "lifeSpan": 10,
      "lifeSpanEndDate": "${today.plusDays(7)}",
      "manufacturer": "Dexcom",
      "name": "Dexcom G7 Sensor",
      "referenceNumber": "REF-DXS7-2024",
      "serialNumber": "ZUFBFBF7976",
      "updatedAt": "$today"
    },
    {
      "batchNumber": "XXXXX",
      "createdAt": "${today.minusYears(1)}",
      "date": "${today.minusYears(1)}",
      "deviceType": "WIRELESS_PATCH_REMOTE",
      "id": 0,
      "isArchived": false,
      "isFaulty": false,
      "isLifeSpanOver": false,
      "isReported": false,
      "lifeSpan": 0,
      "lifeSpanEndDate": "${today.minusYears(1)}",
      "manufacturer": "Insulet",
      "name": "Omnipod 5 Dash",
      "referenceNumber": "",
      "serialNumber": "XXXXXX",
      "updatedAt": "${today.minusYears(1)}"
    },
    {
      "batchNumber": "HEOEB79",
      "createdAt": "${today.minusDays(20)}",
      "date": "${today.minusDays(20)}",
      "deviceType": "CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR",
      "id": 0,
      "isArchived": false,
      "isFaulty": true,
      "isLifeSpanOver": true,
      "isReported": false,
      "lifeSpan": 10,
      "lifeSpanEndDate": "${today.minusDays(10)}",
      "manufacturer": "Dexcom",
      "name": "Dexcom G7 Sensor",
      "referenceNumber": "REF-DXS7-2024",
      "serialNumber": "ZUFBFBF7977",
      "updatedAt": "${today.minusDays(10)}"
    },
    {
      "batchNumber": "DBOENE58",
      "createdAt": "${today.minusDays(15)}",
      "date": "${today.minusDays(15)}",
      "deviceType": "WIRELESS_PATCH",
      "id": 0,
      "isArchived": false,
      "isFaulty": true,
      "isLifeSpanOver": true,
      "isReported": true,
      "lifeSpan": 3,
      "lifeSpanEndDate": "${today.minusDays(12)}",
      "manufacturer": "Insulet",
      "name": "Omnipod pod",
      "referenceNumber": "",
      "serialNumber": "VEODJE5937EBBODLDC",
      "updatedAt": "${today.minusDays(12)}"
    }
  ],
  "hba1c": [
    {
      "createdAt": "${today.minusMonths(12)}",
      "date": "${today.minusMonths(12)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(12)}",
      "value": 8.1
    },
    {
      "createdAt": "${today.minusMonths(9)}",
      "date": "${today.minusMonths(9)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(9)}",
      "value": 7.5
    },
    {
      "createdAt": "${today.minusMonths(6)}",
      "date": "${today.minusMonths(6)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(6)}",
      "value": 7.2
    },
    {
      "createdAt": "${today.minusMonths(3)}",
      "date": "${today.minusMonths(3)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(3)}",
      "value": 6.8
    },
    {
      "createdAt": "$today",
      "date": "$today",
      "id": 0,
      "isArchived": false,
      "updatedAt": "$today",
      "value": 6.5
    }
  ],
  "importantDates": [
    {
      "createdAt": "${today.minusYears(1)}",
      "date": "${today.minusYears(3)}",
      "id": 0,
      "importantDate": "Omnipod installation",
      "isArchived": false,
      "updatedAt": "${today.minusYears(1)}"
    }
  ],
  "treatments": [
    {
      "createdAt": "${today.minusMonths(2)}",
      "expirationDate": "${today.plusDays(16)}",
      "id": 0,
      "isArchived": false,
      "name": "Novorapid Penfill",
      "type": "FAST_ACTING_INSULIN_CARTRIDGE",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(2)}",
      "expirationDate": "${today.plusDays(60)}",
      "id": 0,
      "isArchived": false,
      "name": "Novorapid Vial",
      "type": "FAST_ACTING_INSULIN_VIAL",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(3)}",
      "expirationDate": "${today.plusMonths(8)}",
      "id": 0,
      "isArchived": false,
      "name": "Levemir Penfill",
      "type": "SLOW_ACTING_INSULIN_CARTRIDGE",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(1)}",
      "expirationDate": "${today.plusMonths(3)}",
      "id": 0,
      "isArchived": false,
      "name": "Glucagen Kit",
      "type": "GLUCAGON_SYRINGE",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusMonths(1)}",
      "expirationDate": "${today.plusMonths(5)}",
      "id": 0,
      "isArchived": false,
      "name": "Baqsimi",
      "type": "GLUCAGON_SPRAY",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusWeeks(2)}",
      "expirationDate": "${today.plusMonths(4)}",
      "id": 0,
      "isArchived": false,
      "name": "FreeStyle Optium B-Ketone",
      "type": "B_KETONE_TEST_STRIP",
      "updatedAt": "$today"
    },
    {
      "createdAt": "${today.minusWeeks(2)}",
      "expirationDate": "${today.plusMonths(6)}",
      "id": 0,
      "isArchived": false,
      "name": "FreeStyle Optium Glucose",
      "type": "BLOOD_GLUCOSE_TEST_STRIP",
      "updatedAt": "$today"
    }
  ],
  "userDetails": {
    "basalInsulinType": "Novorapid",
    "birthdate": "1991-06-21",
    "bloodType": "AB_POSITIVE",
    "cardiologist": "Pr. Yolanda Davis",
    "cgmModel": "Dexcom G7",
    "diabetesType": "TYPE_1",
    "diagnosisDate": "${today.minusYears(12)}",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+12481237654",
    "endocrinologist": "Pr. Douglas T. Cobos",
    "firstName": "John",
    "gender": "MALE",
    "generalPractitioner": "Dr. Eric M. Ellison",
    "glucoseUnit": "MG_DL",
    "id": 0,
    "insulinPumpModel": "Omnipod Dash",
    "insulinType": "Novorapid",
    "lastName": "Doe",
    "nephrologist": "Dr. Michele M. Pankey",
    "ophthalmologist": "Dr. Brian Leclair",
    "profilePhotoPath": null,
    "targetGlucoseMax": 180.0,
    "targetGlucoseMin": 60.0
  },
  "weights": [
    {
      "createdAt": "${today.minusMonths(6)}",
      "date": "${today.minusMonths(6)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(6)}",
      "value": 75.5
    },
    {
      "createdAt": "${today.minusMonths(4)}",
      "date": "${today.minusMonths(4)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(4)}",
      "value": 76.2
    },
    {
      "createdAt": "${today.minusMonths(2)}",
      "date": "${today.minusMonths(2)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusMonths(2)}",
      "value": 77.0
    },
    {
      "createdAt": "${today.minusDays(15)}",
      "date": "${today.minusDays(15)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusDays(15)}",
      "value": 76.5
    },
    {
      "createdAt": "${today.minusDays(1)}",
      "date": "${today.minusDays(1)}",
      "id": 0,
      "isArchived": false,
      "updatedAt": "${today.minusDays(1)}",
      "value": 76.8
    }
  ]
}
""".trimIndent()

val outputDir = File("generated_test_data")
outputDir.mkdirs()

println("📂 Output directory: ${outputDir.absolutePath}")

val profilePhoto = File("scripts/profile_photo.jpg")
if (!profilePhoto.exists()) {
    println("⬇️  Downloading placeholder avatar...")
    URL("https://i.pravatar.cc/300").openStream().use { input ->
        profilePhoto.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    println("✅ Avatar saved: ${profilePhoto.absolutePath}")
}

val zipFile = File(outputDir, "diabdata_test_export.zip")
ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
    zip.putNextEntry(ZipEntry("data.json"))
    zip.write(json.toByteArray())
    zip.closeEntry()

    if (profilePhoto.exists()) {
        zip.putNextEntry(ZipEntry("profile_photo.jpg"))
        profilePhoto.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}

println("✅ Test data generated: ${zipFile.absolutePath}")