const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.onCommand = onDocumentCreated("commands/{id}", async (event) => {
  const cmd = event.data.data();
  const dev = await admin.firestore().collection("devices").doc(cmd.deviceCode).get();
  if (!dev.exists) return;

  await admin.messaging().send({
    token: dev.get("token"),
    data: { action: cmd.action },
    android: { priority: "high", ttl: 60 * 1000 },
  });
  await event.data.ref.delete();
});
