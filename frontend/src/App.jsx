import { useEffect, useState } from "react";
import { createComment, getComments, getTickets } from "./api";
import "./App.css";

export default function App() {
  const [text, setText] = useState("");
  const [channel, setChannel] = useState("web");
  const [comments, setComments] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const [nextComments, nextTickets] = await Promise.all([
          getComments(),
          getTickets(),
        ]);
        if (!cancelled) {
          setComments(nextComments);
          setTickets(nextTickets);
        }
      } catch (error) {
        if (!cancelled) {
          setMessage(error.message);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  async function onSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      const result = await createComment(text, channel);
      setText("");
      setMessage(
        result.ticketCreated
          ? `Comment saved. Ticket #${result.ticketId} created.`
          : "Comment saved. No ticket created.",
      );
      const [nextComments, nextTickets] = await Promise.all([
        getComments(),
        getTickets(),
      ]);
      setComments(nextComments);
      setTickets(nextTickets);
    } catch (err) {
      setMessage(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <header>
        <h1>PulseDesk</h1>
        <p>Comment triage demo</p>
      </header>

      <form className="card" onSubmit={onSubmit}>
        <h2>Submit comment</h2>
        <label>
          Channel
          <select value={channel} onChange={(e) => setChannel(e.target.value)}>
            <option value="web">web</option>
            <option value="app-review">app-review</option>
            <option value="chat">chat</option>
          </select>
        </label>
        <label>
          Comment
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            rows={4}
            required
            placeholder="Describe an issue or leave feedback..."
          />
        </label>
        <button type="submit" disabled={loading || !text.trim()}>
          {loading ? "Sending..." : "Submit"}
        </button>
        {message && <p className="status">{message}</p>}
      </form>

      <section className="grid">
        <div className="card">
          <h2>Comments ({comments.length})</h2>
          <ul>
            {comments.map((c) => (
              <li key={c.id}>
                <strong>#{c.id}</strong> [{c.channel || "n/a"}] {c.text}
              </li>
            ))}
          </ul>
        </div>

        <div className="card">
          <h2>Tickets ({tickets.length})</h2>
          <ul>
            {tickets.map((t) => (
              <li key={t.id}>
                <strong>#{t.id}</strong> [{t.priority}] {t.title}
                <div className="meta">
                  {t.category} — {t.summary}
                </div>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
