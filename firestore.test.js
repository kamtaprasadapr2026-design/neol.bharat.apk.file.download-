const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student";
const BOB_UID = "bob_student";
const ADMIN_UID = "admin_super";

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

test("Unauthenticated user: cannot read attendance", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("attendance").get());
});

test("Student cannot read another student's attendance", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("attendance").doc("att_bob").set({
      userId: BOB_UID,
      studentName: "Bob",
      date: "2026-10-07",
      status: "PRESENT"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("attendance").doc("att_bob").get());
});

test("Student can read their own attendance", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("attendance").doc("att_alice").set({
      userId: ALICE_UID,
      studentName: "Alice",
      date: "2026-10-07",
      status: "PRESENT"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("attendance").doc("att_alice").get());
});

test("Student cannot forge verified payment directly", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("payments").doc("fake_pay").set({
    userId: ALICE_UID,
    userName: "Alice",
    amount: 500,
    status: "VERIFIED"
  }));
});

test("Student cannot modify fee records", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("fees").doc("fee_alice").set({
    userId: ALICE_UID,
    pendingAmount: 0,
    status: "PAID"
  }));
});
