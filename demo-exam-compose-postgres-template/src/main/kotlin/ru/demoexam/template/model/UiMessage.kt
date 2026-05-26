package ru.demoexam.template.model

enum class MessageKind {
    INFO,
    WARNING,
    ERROR,
}

data class UiMessage(
    val title: String,
    val text: String,
    val kind: MessageKind,
)

