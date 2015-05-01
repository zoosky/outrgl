package com.outr

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Event, EventListener, Actor}
import com.badlogic.gdx.scenes.scene2d.actions.{AlphaAction, MoveToAction, RunnableAction}
import com.outr.gl.screen.AbstractBaseScreen
import org.powerscala.Unique

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object gl {
  var VirtualWidth = 1920.0f
  var VirtualHeight = 1080.0f

  var WidthOverride: Option[Float] = None
  var HeightOverride: Option[Float] = None

  def ActualWidth = WidthOverride.getOrElse(Gdx.graphics.getWidth.toFloat)
  def ActualHeight = HeightOverride.getOrElse(Gdx.graphics.getHeight.toFloat)

  private def modifier = ActualWidth / VirtualWidth

  def fontSize(originalSize: Int) = math.round(originalSize * modifier)

  def function(f: => Unit) = {
    val runnable = new Runnable {
      override def run() = f
    }
    val action = new RunnableAction
    action.setRunnable(runnable)
    action
  }

  def move(actor: Actor, x: Float, y: Float, duration: Float, interpolation: Interpolation) = {
    val action = new MoveToAction
    action.setX(x)
    action.setY(y)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def fade(actor: Actor, to: Float, duration: Float, interpolation: Interpolation) = {
    val action = new AlphaAction
    action.setAlpha(to)
    action.setDuration(duration)
    action.setInterpolation(interpolation)
    action.setTarget(actor)
    action
  }

  def adjustedWide(value: Float) = value * (ActualWidth / VirtualWidth)
  def adjustedTall(value: Float) = value * (ActualHeight / VirtualHeight)

  implicit class AdjustedActor[A <: Actor](actor: A)(implicit screen: AbstractBaseScreen) {
    if (actor.getName == null) {
      actor.setName(Unique())
    }

    def id = actor.getName

    def positioned(x: Float, y: Float, offsetFromHeight: Boolean = true) = this.x(x).y(y, offsetFromHeight)

    def x(x: Float) = {
      screen.onResize(s"$id:x", () => actor.setX(adjustedWide(x)))
      actor
    }

    def y(y: Float, offsetFromHeight: Boolean = true) = {
      screen.onResize(s"$id:y", () => {
        if (offsetFromHeight) {
          actor.setY(ActualHeight - adjustedTall(y) - actor.getHeight)
        } else {
          actor.setY(adjustedTall(y))
        }
      })
      actor
    }

    def right(x: Float) = {
      screen.onResize(s"$id:x", () => actor.setX(adjustedWide(x) - actor.getWidth))
      actor
    }

    def alpha(a: Float) = {
      actor.getColor.a = a
      actor
    }

    def color(color: Color) = {
      actor.setColor(color)
      actor
    }

    def sized(width: Float, height: Float) = {
      this.width(width).height(height)
      actor
    }

    def width(width: Float = VirtualWidth) = {
      screen.onResize(s"$id:width", () => actor.setWidth(adjustedWide(width)))
      actor
    }

    def height(height: Float = VirtualHeight) = {
      screen.onResize(s"$id:height", () => actor.setHeight(adjustedTall(height)))
      actor
    }

    def centerX() = {
      screen.onResize(s"$id:x", () => actor.setX((ActualWidth / 2.0f) - (actor.getWidth / 2.0f)))
      actor
    }

    def centerY() = {
      screen.onResize(s"$id:y", () => actor.setY((ActualHeight / 2.0f) - (actor.getHeight / 2.0f)))
      actor
    }

    def center() = centerX().centerY()

    def onTouch(f: => Unit) = {
      actor.addListener(new EventListener {
        override def handle(event: Event) = event match {
          case e: InputEvent if e.getType == InputEvent.Type.touchDown => {
            f
            true
          }
          case _ => false
        }
      })
    }
  }
}
