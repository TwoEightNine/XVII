package global.msnthrp.xvii.uikit.extensions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View


fun View.fadeOut(duration: Long, onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

fun View.fadeIn(duration: Long, onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

open class StubAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {

    }

    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {

    }

    override fun onAnimationEnd(animation: Animator?) {

    }

    override fun onAnimationCancel(animation: Animator?) {

    }

    override fun onAnimationStart(animation: Animator?) {

    }
}

open class EndAnimatorListener(private val onEnd: () -> Unit) : StubAnimatorListener() {

    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
        super.onAnimationEnd(animation, isReverse)
        onEnd()
    }

    override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        onEnd()
    }
}