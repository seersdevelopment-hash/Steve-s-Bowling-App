# Bowler Speed

Android MVP for ball-by-ball cricket bowling speed capture from a single phone camera.

This build follows the supplied spec's first implementation milestone:

- Jetpack Compose app shell with Home, setup, calibration, recording, result, summary, and history screens.
- CameraX rear-camera preview and per-ball video recording.
- Local video files saved under the app's external movies directory.
- Room storage for sessions, deliveries, and calibration metadata.
- Mocked speed and confidence result after each recorded delivery, ready to replace with a real ball-tracking model.

## Open In Android Studio

1. Open this folder: `/Users/stevenlewis/Documents/Bowling App`
2. Let Android Studio sync the Gradle project.
3. Run the `app` configuration on an Android phone or emulator with a camera.

## Current MVP Behavior

Create a session, walk through the setup guide, enter calibration values, record a delivery, stop recording, and the app saves a delivery row with a mocked speed result and confidence score. Session history remains local through Room.

## Next Engineering Step

Replace `MockSpeedEstimator` with the real speed estimation pipeline:

1. Detect pitch/stump geometry during setup.
2. Track the ball across frames in the recorded delivery.
3. Convert frame movement to metres using calibration.
4. Compute speed from distance over timestamps.
5. Flag low-confidence deliveries when ball tracking is lost or occluded.