package evgeny.example.admin.wordpronucation.training_part

interface UiListener {
    abstract fun setVisibilityStartTextView(v: Int)

    abstract fun setVisibilityCurrentWordFrameLayout(v: Int)

    abstract fun setVisibilityRippleAnimationView(v: Int)

    abstract fun setTextCurrentResultTextView(text: String)

    abstract fun setTextCurrentWordTextView(text: String)

    abstract fun setTextCurrentTranslatedWordTextView(text: String)

    abstract fun setTextStartTextView(text: String)

    abstract fun setProgress(progress: Float)

    abstract fun updateSharedPref()

    abstract fun setProgressBarForegroundStrokeWidth(width: Float)
}