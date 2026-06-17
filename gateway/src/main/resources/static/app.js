const chatForm = document.getElementById("chatForm");
const messageInput = document.getElementById("messageInput");
const messageArea = document.getElementById("messageArea");

chatForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const text = messageInput.value.trim();

    if (!text) {
        return;
    }

    addMessage(text, "user");
    messageInput.value = "";

    setTimeout(function () {
        addMessage("Thanks for your question. The course assistant backend can be connected here.", "assistant");
    }, 300);
});

function addMessage(text, sender) {
    const message = document.createElement("div");
    const paragraph = document.createElement("p");

    message.className = "message " + sender + "-message";
    paragraph.textContent = text;

    message.appendChild(paragraph);
    messageArea.appendChild(message);
    messageArea.scrollTop = messageArea.scrollHeight;
}
