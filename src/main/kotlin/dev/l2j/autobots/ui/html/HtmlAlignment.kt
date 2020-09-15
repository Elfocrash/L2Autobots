package dev.l2j.autobots.ui.html

internal enum class HtmlAlignment(val alignment: String) {
    Center("center"),
    Left("left"),
    Right("right");

    override fun toString(): String {
        return alignment
    }
}