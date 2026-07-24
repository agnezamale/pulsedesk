const API_URL = "http://localhost:8080";

export async function getComments() {
    const res = await fetch(`${API_URL}/comments`);
    if(!res.ok) throw new Error("Failed to load comments");
    return res.json();
}

export async function createComment(text, channel) {
    const res = await fetch(`${API_URL}/comments`, {
        method: "POST", 
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({text, channel}),
    });
    if (!res.ok) throw new Error("Failed to create comment");
    return res.json();
}

export async function getTickets() {
    const res = await fetch(`${API_URL}/tickets`);
    if(!res.ok) throw new Error("failed to load tickets");
    return res.json();
}