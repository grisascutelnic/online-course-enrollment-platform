const loginView = document.getElementById("loginView");
const chatView = document.getElementById("chatView");
const loginForm = document.getElementById("loginForm");
const usernameInput = document.getElementById("usernameInput");
const passwordInput = document.getElementById("passwordInput");
const loginError = document.getElementById("loginError");
const loginButton = document.getElementById("loginButton");
const logoutButton = document.getElementById("logoutButton");
const chatForm = document.getElementById("chatForm");
const messageInput = document.getElementById("messageInput");
const messageArea = document.getElementById("messageArea");
const sendButton = chatForm.querySelector("button");
const initialMessageMarkup = messageArea.innerHTML;

const tokenKey = "jwtToken";

if (localStorage.getItem(tokenKey)) {
    showChat();
} else {
    showLogin();
}

loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username || !password) {
        showLoginError("Please enter your username and password.");
        return;
    }

    loginButton.disabled = true;
    loginButton.textContent = "Logging in...";
    showLoginError("");

    try {
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (!response.ok) {
            throw new Error("Invalid username or password.");
        }

        const data = await response.json();

        if (!data.token) {
            throw new Error("Login succeeded, but no token was returned.");
        }

        localStorage.setItem(tokenKey, data.token);
        passwordInput.value = "";
        resetConversation();
        showChat();
    } catch (error) {
        showLoginError(error.message || "Login failed. Please try again.");
    } finally {
        loginButton.disabled = false;
        loginButton.textContent = "Login";
    }
});

logoutButton.addEventListener("click", function () {
    localStorage.removeItem(tokenKey);
    resetConversation();
    showLogin();
});

chatForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const text = messageInput.value.trim();

    if (!text) {
        return;
    }

    addMessage(text, "user");
    messageInput.value = "";
    setChatFormLoading(true);

    const loadingMessage = addLoadingMessage();
    const token = localStorage.getItem(tokenKey);

    if (!token) {
        removeLoadingMessage(loadingMessage);
        setChatFormLoading(false);
        showLogin();
        showLoginError("Please login before using the assistant.");
        return;
    }

    try {
        const response = await fetch("/ai/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({
                message: text
            })
        });

        if (response.status === 401 || response.status === 403) {
            removeLoadingMessage(loadingMessage);
            localStorage.removeItem(tokenKey);
            resetConversation();
            showLogin();
            showLoginError("Your session expired. Please login again.");
            return;
        }

        if (!response.ok) {
            throw new Error("AI assistant failed to respond. Status: " + response.status);
        }

        const data = await response.json();
        removeLoadingMessage(loadingMessage);
        addMessage(data.answer, "assistant");

    } catch (error) {
        removeLoadingMessage(loadingMessage);
        addMessage(error.message || "Something went wrong.", "assistant");
    } finally {
        setChatFormLoading(false);
    }
});

function addMessage(text, sender) {
    const message = document.createElement("div");

    message.className = "message " + sender + "-message";
    appendFormattedBlocks(message, text);

    messageArea.appendChild(message);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function appendFormattedBlocks(parent, text) {
    const lines = normalizeMarkdownText(text)
        .split(/\r?\n/)
        .reduce(function (allLines, line) {
            return allLines.concat(splitInlineTableRows(line));
        }, []);
    let paragraphLines = [];
    let list = null;
    let tableLines = [];

    function flushCurrentParagraph() {
        flushParagraph(parent, paragraphLines);
        paragraphLines = [];
    }

    function flushCurrentTable() {
        flushTable(parent, tableLines);
        tableLines = [];
    }

    lines.forEach(function (line) {
        const trimmedLine = line.trim();
        const headingMatch = trimmedLine.match(/^(#{1,6})\s+(.+)$/);
        const separatorMatch = trimmedLine.match(/^(-{3,}|\*{3,}|_{3,})$/);
        const tableMatch = trimmedLine.match(/^\|(.+)\|$/);
        const bulletMatch = trimmedLine.match(/^[-*]\s+(.+)$/);
        const numberedMatch = trimmedLine.match(/^\d+\.\s+(.+)$/);

        if (!trimmedLine) {
            flushCurrentParagraph();
            flushCurrentTable();
            list = null;
            return;
        }

        if (trimmedLine === "-") {
            flushCurrentParagraph();
            list = null;
            return;
        }

        if (headingMatch) {
            flushCurrentParagraph();
            flushCurrentTable();
            list = null;

            const level = Math.min(6, Math.max(3, headingMatch[1].length));
            const heading = document.createElement("h" + level);

            appendInlineFormattedText(heading, headingMatch[2]);
            parent.appendChild(heading);
            return;
        }

        if (separatorMatch) {
            flushCurrentParagraph();
            flushCurrentTable();
            list = null;
            parent.appendChild(document.createElement("hr"));
            return;
        }

        if (tableMatch) {
            flushCurrentParagraph();
            list = null;
            tableLines.push(trimmedLine);
            return;
        }

        if (bulletMatch || numberedMatch) {
            flushCurrentParagraph();
            flushCurrentTable();

            const listType = numberedMatch ? "ol" : "ul";

            if (!list || list.tagName.toLowerCase() !== listType) {
                list = document.createElement(listType);
                parent.appendChild(list);
            }

            const item = document.createElement("li");

            appendInlineFormattedText(item, bulletMatch ? bulletMatch[1] : numberedMatch[1]);
            list.appendChild(item);
            return;
        }

        flushCurrentTable();
        paragraphLines.push(trimmedLine);
        list = null;
    });

    flushCurrentParagraph();
    flushCurrentTable();
}

function normalizeMarkdownText(text) {
    return String(text || "")
        .replace(/\r\n/g, "\n")
        .replace(/\u00a0/g, " ")
        .replace(/\s+(?=(?:\*\*)?(Category|Difficulty|Duration|Available Seats|Status|Description|Prerequisites|Skills You Will Learn|Course Modules|Total estimated study time|Total estimated hours)(?:\*\*)?:)/gi, "\n")
        .replace(/\s*-\s*\n(?=(?:\*\*)?(Category|Difficulty|Duration|Available Seats|Status)(?:\*\*)?:)/gi, "\n");
}

function splitInlineTableRows(line) {
    const tableRowSeparator = /\|\s*\|/;

    if (!tableRowSeparator.test(line)) {
        return [line];
    }

    return line
        .split(tableRowSeparator)
        .map(function (row) {
            let tableRow = row.trim();

            if (!tableRow) {
                return "";
            }

            if (!tableRow.startsWith("|")) {
                tableRow = "| " + tableRow;
            }

            if (!tableRow.endsWith("|")) {
                tableRow = tableRow + " |";
            }

            return tableRow;
        })
        .filter(function (row) {
            return row;
        });
}

function flushParagraph(parent, paragraphLines) {
    if (!paragraphLines.length) {
        return;
    }

    const paragraph = document.createElement("p");

    appendInlineFormattedText(paragraph, paragraphLines.join(" "));
    parent.appendChild(paragraph);
}

function flushTable(parent, tableLines) {
    let rows = tableLines
        .map(parseTableRow)
        .filter(function (row) {
            return row.length && !row.every(isDividerCell);
        });

    if (!rows.length) {
        return;
    }

    rows = normalizeComparisonTableRows(rows);

    const table = document.createElement("table");
    const body = document.createElement("tbody");

    rows.forEach(function (row, rowIndex) {
        const tableRow = document.createElement("tr");
        const cellTag = rowIndex === 0 ? "th" : "td";

        row.forEach(function (cellText) {
            const cell = document.createElement(cellTag);

            appendInlineFormattedText(cell, cellText);
            tableRow.appendChild(cell);
        });

        body.appendChild(tableRow);
    });

    table.appendChild(body);
    parent.appendChild(table);
}

function normalizeComparisonTableRows(rows) {
    const maxColumns = Math.max.apply(null, rows.map(function (row) {
        return row.length;
    }));

    if (rows.length > 1 && rows[0].length === maxColumns - 1) {
        return rows.map(function (row, rowIndex) {
            if (rowIndex === 0) {
                return [""].concat(row);
            }

            return row;
        });
    }

    return rows;
}

function parseTableRow(line) {
    return line
        .replace(/^\|/, "")
        .replace(/\|$/, "")
        .split("|")
        .map(function (cell) {
            return cell.trim();
        });
}

function isDividerCell(cell) {
    return /^:?-{3,}:?$/.test(cell);
}

function appendInlineFormattedText(parent, text) {
    const parts = String(text || "").split(/(\*\*[^*]+\*\*)/g);

    parts.forEach(function (part) {
        if (part.startsWith("**") && part.endsWith("**") && part.length > 4) {
            const strong = document.createElement("strong");
            const strongText = part.slice(2, -2);

            appendBreakBeforeFieldLabel(parent, strongText);
            strong.textContent = strongText;
            parent.appendChild(strong);
            return;
        }

        appendPlainTextWithFieldBreaks(parent, part);
    });
}

function appendPlainTextWithFieldBreaks(parent, text) {
    const fieldPattern = /(Category|Difficulty|Duration|Available Seats|Status|Description|Prerequisites|Skills You Will Learn|Course Modules|Total estimated study time|Total estimated hours):/gi;
    const parts = String(text || "").split(fieldPattern);

    if (parts.length === 1) {
        parent.appendChild(document.createTextNode(text));
        return;
    }

    parent.appendChild(document.createTextNode(parts[0]));

    for (let i = 1; i < parts.length; i += 2) {
        const label = parts[i] + ":";
        const value = parts[i + 1] || "";
        const strong = document.createElement("strong");

        appendBreakBeforeFieldLabel(parent, label);
        strong.textContent = label;
        parent.appendChild(strong);
        parent.appendChild(document.createTextNode(value));
    }
}

function appendBreakBeforeFieldLabel(parent, label) {
    if (!isFieldLabel(label) || !parent.textContent.trim()) {
        return;
    }

    const lastChild = parent.lastChild;

    if (!lastChild || lastChild.nodeName === "BR") {
        return;
    }

    parent.appendChild(document.createElement("br"));
}

function isFieldLabel(label) {
    return /^(Category|Difficulty|Duration|Available Seats|Status|Description|Prerequisites|Skills You Will Learn|Course Modules|Total estimated study time|Total estimated hours):$/i
        .test(String(label || "").trim());
}

function addLoadingMessage() {
    const message = document.createElement("div");
    const spinner = document.createElement("div");
    const status = document.createElement("span");

    message.className = "message assistant-message loading-message";
    message.setAttribute("aria-label", "Assistant is typing");
    spinner.className = "spinner";
    status.className = "sr-only";
    status.textContent = "Assistant is typing";

    message.appendChild(spinner);
    message.appendChild(status);
    messageArea.appendChild(message);
    messageArea.scrollTop = messageArea.scrollHeight;

    return message;
}

function removeLoadingMessage(loadingMessage) {
    if (loadingMessage) {
        loadingMessage.remove();
    }
}

function setChatFormLoading(isLoading) {
    messageInput.disabled = isLoading;
    sendButton.disabled = isLoading;
    sendButton.textContent = isLoading ? "Sending..." : "Send";
}

function showChat() {
    loginView.classList.add("hidden");
    chatView.classList.remove("hidden");
    messageInput.focus();
}

function showLogin() {
    chatView.classList.add("hidden");
    loginView.classList.remove("hidden");
    showLoginError("");
    usernameInput.focus();
}

function resetConversation() {
    messageArea.innerHTML = initialMessageMarkup;
    messageInput.value = "";
    setChatFormLoading(false);
}

function showLoginError(message) {
    loginError.textContent = message;
}
