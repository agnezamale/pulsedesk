const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export async function getComments() {
  const res = await fetch(`${API_URL}/comments`);
  if (!res.ok) throw new Error("Failed to load comments");
  return res.json();
}

export async function createComment(text, channel) {
  const res = await fetch(`${API_URL}/comments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ text, channel }),
  });
  if (!res.ok) throw new Error("Failed to create comment");
  return res.json();
}

export async function getTickets() {
  const res = await fetch(`${API_URL}/tickets`);
  if (!res.ok) throw new Error("Failed to load tickets");
  return res.json();
}

export async function getTriageStatus(commentId) {
  const res = await fetch(`${API_URL}/comments/${commentId}/triage-status`);
  if (!res.ok) throw new Error("Failed to load triage status");
  return res.json();
}

export async function waitForTriage(commentId, { intervalMs = 1000, timeoutMs = 60_000 } = {}) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    const status = await getTriageStatus(commentId);
    if (status.triageStatus === "COMPLETED") {
      return status;
    }
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
  throw new Error("Triage timed out");
}
