import { useState, useRef, useEffect } from "react";
import { agentChat, requestAgentChat, orchestratorChat } from "../services/api";
import "./AgentChatPage.css";

const AgentChatPage = () => {
  const role = localStorage.getItem("role");
  const [activeAgent, setActiveAgent] = useState("orchestrator");
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const chatEndRef = useRef(null);

  const agentInfo = {
    orchestrator: {
      name: "🧠 Smart Assistant",
      welcome:
        role === "ADMIN"
          ? "👋 Hi Admin! I'm your Smart Assistant. I automatically route your requests!\n\n🛡️ User Management: Create, find, delete, edit users\n📋 Admin Requests: View, approve, reject requests\n\nJust ask me anything!"
          : "👋 Hi! I'm your Smart Assistant.\n\n🔍 I can help you search and view users.\n⚠️ For create/delete operations, you need ADMIN access.\n\nJust ask me anything!",
    },
    "user-admin": {
      name: "🛡️ User Admin Agent",
      welcome:
        "👋 I'm the User Admin Agent.\n\n• Show all users\n• Find by name/email/city\n• User statistics",
    },
    "admin-requests": {
      name: "📋 Admin Request Agent",
      welcome:
        "👋 I'm the Admin Request Agent.\n\n• Show pending requests\n• Approve/reject requests\n• Request statistics",
    },
  };

  useEffect(() => {
    setMessages([{ sender: "agent", text: agentInfo[activeAgent].welcome }]);
  }, [activeAgent]);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput("");

    setMessages((prev) => [...prev, { sender: "user", text: userMessage }]);
    setLoading(true);

    try {
      let response;
      if (activeAgent === "orchestrator") {
        response = await orchestratorChat(userMessage);
      } else if (activeAgent === "user-admin") {
        response = await agentChat(userMessage);
      } else {
        response = await requestAgentChat(userMessage);
      }

      const agentResponse =
        response.data.response || "I couldn't process that. Please try again.";

      setMessages((prev) => [
        ...prev,
        { sender: "agent", text: agentResponse },
      ]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          sender: "agent",
          text:
            "❌ Error: " +
            (err.response?.data?.error || "Something went wrong!"),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h2>{agentInfo[activeAgent].name}</h2>

        <div className="agent-selector">
          <button
            onClick={() => setActiveAgent("orchestrator")}
            className={`agent-tab ${activeAgent === "orchestrator" ? "active-tab" : ""}`}
          >
            🧠 Smart
          </button>
          <button
            onClick={() => setActiveAgent("user-admin")}
            className={`agent-tab ${activeAgent === "user-admin" ? "active-tab" : ""}`}
          >
            🛡️ Users
          </button>
          {role === "ADMIN" && (
            <button
              onClick={() => setActiveAgent("admin-requests")}
              className={`agent-tab ${activeAgent === "admin-requests" ? "active-tab" : ""}`}
            >
              📋 Requests
            </button>
          )}
        </div>
      </div>

      <div className="chat-messages">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`chat-bubble ${msg.sender === "user" ? "user-bubble" : "agent-bubble"}`}
          >
            <span className="bubble-label">
              {msg.sender === "user" ? "You" : "🤖 Agent"}
            </span>
            <p className="bubble-text">{msg.text}</p>
          </div>
        ))}

        {loading && (
          <div className="chat-bubble agent-bubble">
            <span className="bubble-label">🤖 Agent</span>
            <p className="bubble-text typing">Thinking...</p>
          </div>
        )}

        <div ref={chatEndRef} />
      </div>

      <div className="chat-input-area">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Type your message..."
          className="chat-input"
          disabled={loading}
        />
        <button
          onClick={handleSend}
          disabled={loading || !input.trim()}
          className="send-btn"
        >
          {loading ? "⏳" : "Send ➤"}
        </button>
      </div>
    </div>
  );
};

export default AgentChatPage;
