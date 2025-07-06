package com.anhq.smartalarm.core.firebase

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import com.anhq.smartalarm.core.model.Alarm
import com.anhq.smartalarm.core.model.AlarmGameType
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.model.Timer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    fun initGoogleSignIn(activity: Activity) {
        Log.d("FirebaseAuthHelper", "Initializing Google Sign In")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("435300733841-707fs4ki47nle1fobgku94l1fp7687q7.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut()
    }

    fun getSignInIntent(): Intent {
        Log.d("FirebaseAuthHelper", "Getting sign in intent")
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            Log.d("FirebaseAuthHelper", "Handling sign in result: $data")
            if (data == null) {
                Log.e("FirebaseAuthHelper", "Sign in data is null")
                onFailure(Exception("Sign in data is null"))
                return
            }

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d("FirebaseAuthHelper", "Getting account from intent")
            val account = task.await()
            Log.d("FirebaseAuthHelper", "Got account: ${account.email}")
            firebaseAuthWithGoogle(account, onSuccess, onFailure)
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error in handleSignInResult", e)
            onFailure(e)
        }
    }

    private suspend fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            Log.d("FirebaseAuthHelper", "Starting Firebase auth with Google")
            val idToken = account.idToken
            if (idToken == null) {
                Log.e("FirebaseAuthHelper", "ID Token is null")
                onFailure(Exception("ID Token is null"))
                return
            }
            Log.d("FirebaseAuthHelper", "Got ID token, creating credential")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            Log.d("FirebaseAuthHelper", "Signing in with credential")

            val result = auth.signInWithCredential(credential).await()
            Log.d("FirebaseAuthHelper", "Sign in successful: ${result.user?.email}")
            onSuccess()
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error in firebaseAuthWithGoogle", e)
            onFailure(e)
        }
    }

    suspend fun saveAlarmData(alarm: Alarm) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            Log.d("FirebaseAuthHelper", "Starting to save alarm ${alarm.id} to user: $userId")

            val existingAlarms = db.collection("users")
                .document(userId)
                .collection("alarms")
                .get()
                .await()

            val duplicateDoc = existingAlarms.documents.find { doc ->
                val hour = doc.getLong("hour")?.toInt()
                val minute = doc.getLong("minute")?.toInt()
                val selectedDays = (doc.get("selectedDays") as? List<*>)?.map { it.toString() }?.toSet()

                hour == alarm.hour &&
                minute == alarm.minute &&
                selectedDays == alarm.selectedDays.map { it.name }.toSet()
            }

            val alarmMap = mapOf(
                "id" to alarm.id,
                "hour" to alarm.hour,
                "minute" to alarm.minute,
                "isActive" to alarm.isActive,
                "isVibrate" to alarm.isVibrate,
                "selectedDays" to alarm.selectedDays.map { it.name },
                "label" to alarm.label,
                "gameType" to alarm.gameType.name,
                "soundUri" to alarm.soundUri
            )

            if (duplicateDoc != null) {
                Log.d("FirebaseAuthHelper", "Found duplicate alarm, updating existing document")
                db.collection("users")
                    .document(userId)
                    .collection("alarms")
                    .document(duplicateDoc.id)
                    .set(alarmMap)
                    .await()
            } else {
                Log.d("FirebaseAuthHelper", "No duplicate found, creating new document")
                db.collection("users")
                    .document(userId)
                    .collection("alarms")
                    .add(alarmMap)
                    .await()
            }

            Log.d("FirebaseAuthHelper", "Successfully saved alarm ${alarm.id}")
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error saving alarm data", e)
            throw e
        }
    }

    suspend fun saveTimerData(timer: Timer) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            Log.d("FirebaseAuthHelper", "Starting to save timer ${timer.id} to user: $userId")

            val existingTimers = db.collection("users")
                .document(userId)
                .collection("timers")
                .get()
                .await()

            val duplicateDoc = existingTimers.documents.find { doc ->
                val initialTimeMillis = doc.getLong("initialTimeMillis")
                val remainingTimeMillis = doc.getLong("remainingTimeMillis")

                initialTimeMillis == timer.initialTimeMillis &&
                remainingTimeMillis == timer.remainingTimeMillis
            }

            val timerMap = mapOf(
                "id" to timer.id,
                "initialTimeMillis" to timer.initialTimeMillis,
                "currentInitialTimeMillis" to timer.currentInitialTimeMillis,
                "remainingTimeMillis" to timer.remainingTimeMillis,
                "isRunning" to timer.isRunning,
                "isPaused" to timer.isPaused,
                "createdAt" to timer.createdAt,
                "endedAt" to timer.endedAt,
                "lastTickTime" to timer.lastTickTime
            )

            if (duplicateDoc != null) {
                Log.d("FirebaseAuthHelper", "Found duplicate timer, updating existing document")
                db.collection("users")
                    .document(userId)
                    .collection("timers")
                    .document(duplicateDoc.id)
                    .set(timerMap)
                    .await()
            } else {
                Log.d("FirebaseAuthHelper", "No duplicate found, creating new document")
                db.collection("users")
                    .document(userId)
                    .collection("timers")
                    .add(timerMap)
                    .await()
            }

            Log.d("FirebaseAuthHelper", "Successfully saved timer ${timer.id}")
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error saving timer data", e)
            throw e
        }
    }

    suspend fun getAlarmData(): List<Alarm> {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            Log.d("FirebaseAuthHelper", "Getting alarms for user: $userId")

            val snapshot = db.collection("users")
                .document(userId)
                .collection("alarms")
                .get()
                .await()

            return snapshot.documents.mapNotNull { doc ->
                try {
                    val hour = doc.getLong("hour")?.toInt() ?: return@mapNotNull null
                    val minute = doc.getLong("minute")?.toInt() ?: return@mapNotNull null
                    val selectedDays = (doc.get("selectedDays") as? List<*>)?.mapNotNull {
                        try {
                            DayOfWeek.valueOf(it.toString())
                        } catch (e: Exception) {
                            null
                        }
                    }?.toSet() ?: emptySet()

                    Alarm(
                        id = 0,
                        hour = hour,
                        minute = minute,
                        isActive = false,
                        isVibrate = doc.getBoolean("isVibrate") ?: true,
                        selectedDays = selectedDays,
                        label = doc.getString("label") ?: "Báo thức",
                        gameType = AlarmGameType.valueOf(doc.getString("gameType") ?: "NONE"),
                        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseAuthHelper", "Error parsing alarm document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error getting alarm data", e)
            throw e
        }
    }

    suspend fun getTimerData(): List<Timer> {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            Log.d("FirebaseAuthHelper", "Getting timers for user: $userId")

            val snapshot = db.collection("users")
                .document(userId)
                .collection("timers")
                .get()
                .await()

            return snapshot.documents.mapNotNull { doc ->
                try {
                    val initialTimeMillis = doc.getLong("initialTimeMillis") ?: return@mapNotNull null
                    val remainingTimeMillis = doc.getLong("remainingTimeMillis") ?: initialTimeMillis

                    Timer(
                        id = 0,
                        initialTimeMillis = initialTimeMillis,
                        currentInitialTimeMillis = doc.getLong("currentInitialTimeMillis") ?: initialTimeMillis,
                        remainingTimeMillis = remainingTimeMillis,
                        isRunning = false,
                        isPaused = true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        endedAt = doc.getLong("endedAt"),
                        lastTickTime = doc.getLong("lastTickTime") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseAuthHelper", "Error parsing timer document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error getting timer data", e)
            throw e
        }
    }

    suspend fun hasExistingData(): Boolean {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val alarms = db.collection("users")
                .document(userId)
                .collection("alarms")
                .get()
                .await()

            val timers = db.collection("users")
                .document(userId)
                .collection("timers")
                .get()
                .await()

            return !alarms.isEmpty || !timers.isEmpty
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error checking existing data", e)
            throw e
        }
    }

    suspend fun deleteAllData() {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val alarms = db.collection("users")
                .document(userId)
                .collection("alarms")
                .get()
                .await()

            for (alarm in alarms) {
                alarm.reference.delete().await()
            }

            val timers = db.collection("users")
                .document(userId)
                .collection("timers")
                .get()
                .await()

            for (timer in timers) {
                timer.reference.delete().await()
            }

            Log.d("FirebaseAuthHelper", "Successfully deleted all data")
        } catch (e: Exception) {
            Log.e("FirebaseAuthHelper", "Error deleting data", e)
            throw e
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun signOut() {
        auth.signOut()
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut()
        }
    }
}
