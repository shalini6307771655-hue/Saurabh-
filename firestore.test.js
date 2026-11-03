const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- USER PROFILE TESTS ---

test("Unauthenticated: cannot read or write user profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    username: "alice",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

test("Authenticated: Alice can create and read her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice Doe",
    username: "alicedoe",
    bio: "Hey there! I am using ChatFlow",
    status: "online",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));

  const doc = await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated: Alice cannot write Bob's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    displayName: "Bob Fake",
    username: "bobfake",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

// --- CONVERSATION TESTS ---

test("Authenticated: Alice can create conversation including herself", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").set({
    type: "direct",
    participantIds: [ALICE_UID, BOB_UID],
    lastMessageText: "Hello Bob!",
    lastMessageSenderId: ALICE_UID,
    lastMessageSenderName: "Alice",
    lastMessageTimestamp: new Date(),
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

test("Authenticated: Charlie (non-participant) cannot read Alice and Bob conversation", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_1").set({
      type: "direct",
      participantIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("conversations").doc("conv_1").get());
});

test("Authenticated: Alice can read conversation she participates in", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_1").set({
      type: "direct",
      participantIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").get());
});

// --- MESSAGE TESTS ---

test("Authenticated: Alice can send message in conversation where she is participant", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
    conversationId: "conv_1",
    senderId: ALICE_UID,
    senderName: "Alice",
    text: "Hey Bob, how are you?",
    participantIds: [ALICE_UID, BOB_UID],
    mediaType: "none",
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});

test("Authenticated: Charlie cannot send message or read messages in Alice and Bob conversation", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
      conversationId: "conv_1",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Secret discussion",
      participantIds: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").get());
  await assertFails(charlieDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_2").set({
    conversationId: "conv_1",
    senderId: CHARLIE_UID,
    senderName: "Charlie",
    text: "I am intruding",
    participantIds: [ALICE_UID, BOB_UID],
    createdAt: new Date(),
    updatedAt: new Date(),
  }));
});
