package juuxel.adorn.client.gui.screen

import com.mojang.blaze3d.systems.RenderSystem
import juuxel.adorn.AdornCommon
import juuxel.adorn.platform.PlatformBridges
import juuxel.adorn.util.Colors
import juuxel.adorn.util.color
import net.minecraft.client.gui.screen.NoticeScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.CyclingButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.OrderableTooltip
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3f
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.reflect.KMutableProperty

abstract class AbstractConfigScreen(title: Text, private val parent: Screen) : Screen(title) {
    private val random: Random = Random.Default
    private val hearts: MutableList<Heart> = ArrayList()
    private var restartRequired = false

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        renderHearts(matrices, delta)
        drawCenteredText(matrices, textRenderer, title, width / 2, 20, Colors.WHITE)
        super.render(matrices, mouseX, mouseY, delta)

        for (child in children()) {
            if (child.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && child is OrderableTooltip) {
                renderOrderedTooltip(matrices, child.orderedTooltip, mouseX, mouseY)
                break
            }
        }
    }

    private fun renderHearts(matrices: MatrixStack, delta: Float) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
        RenderSystem.setShaderTexture(0, HEART_TEXTURE)

        for (heart in hearts) {
            RenderSystem.setShaderColor(Colors.redOf(heart.color), Colors.greenOf(heart.color), Colors.blueOf(heart.color), 1f)
            matrices.push()
            matrices.translate(heart.x.toDouble(), MathHelper.lerp(delta.toDouble(), heart.previousY, heart.y), 0.0)
            matrices.translate(HEART_SIZE.toDouble() / 2, HEART_SIZE.toDouble() / 2, 0.0)
            matrices.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(heart.angle.toFloat()))
            matrices.translate(-HEART_SIZE.toDouble() / 2, -HEART_SIZE.toDouble() / 2, 0.0)
            drawTexture(matrices, 0, 0, HEART_SIZE, HEART_SIZE, 0f, 0f, 8, 8, 8, 8)
            matrices.pop()
        }
    }

    override fun close() {
        client!!.setScreen(
            if (restartRequired) {
                NoticeScreen(
                    { client!!.setScreen(parent) },
                    Text.translatable("gui.adorn.config.restart_required.title"),
                    Text.translatable("gui.adorn.config.restart_required.message"),
                    Text.translatable("gui.ok")
                )
            } else {
                parent
            }
        )
    }

    override fun tick() {
        val iter = hearts.iterator()
        while (iter.hasNext()) {
            val heart = iter.next()

            if (heart.y - HEART_SIZE > height) {
                iter.remove()
            } else {
                heart.move()
            }
        }

        if (random.nextInt(25) == 0) {
            val x = random.nextInt(width)
            val color = HEART_COLORS.random(random)
            val speed = random.nextDouble() * 4 + 0.2
            val angularSpeed = random.nextDouble() * 2 * MAX_HEART_SPEED - MAX_HEART_SPEED
            hearts += Heart(x, -HEART_SIZE.toDouble(), color, speed, angularSpeed)
        }
    }

    private fun wrapTooltipLines(text: Text) =
        textRenderer.wrapLines(text, 200)

    protected fun createConfigToggle(
        x: Int, y: Int, width: Int, property: KMutableProperty<Boolean>, restartRequired: Boolean = false
    ): CyclingButtonWidget<Boolean> = CyclingButtonWidget.onOffBuilder(property.getter.call())
        .tooltip {
            buildList {
                addAll(wrapTooltipLines(Text.translatable(getTooltipTranslationKey(property.name))))

                if (restartRequired) {
                    addAll(
                        wrapTooltipLines(
                            Text.translatable("gui.adorn.config.requires_restart").formatted(Formatting.ITALIC, Formatting.GOLD)
                        )
                    )
                }
            }
        }
        .build(x, y, width, 20, Text.translatable(getOptionTranslationKey(property.name))) { _, value ->
            property.setter.call(value)
            PlatformBridges.configManager.save()

            if (restartRequired) {
                this.restartRequired = true
            }
        }

    protected open fun getOptionTranslationKey(name: String): String =
        "gui.adorn.config.option.$name"

    private fun getTooltipTranslationKey(name: String): String =
        "${getOptionTranslationKey(name)}.description"

    companion object {
        const val BUTTON_HEIGHT = 20
        const val BUTTON_GAP = 4
        const val BUTTON_SPACING = BUTTON_HEIGHT + BUTTON_GAP
        private const val HEART_SIZE = 12
        private val HEART_COLORS: List<Int> = listOf(
            color(0xFF0000), // Red
            color(0xFC8702), // Orange
            color(0xFFFF00), // Yellow
            color(0xA7FC58), // Green
            color(0x2D61FC), // Blue
            color(0xA002FC), // Purple
            color(0x58E9FC), // Light blue
            color(0xFCA1DF), // Pink
        )
        private val HEART_TEXTURE = AdornCommon.id("textures/gui/heart.png")
        private const val MAX_HEART_SPEED = 0.2
    }

    private class Heart(val x: Int, var y: Double, val color: Int, val speed: Double, val angularSpeed: Double) {
        var previousY: Double = y
        var previousAngle: Double = 0.0
        var angle: Double = 0.0

        fun move() {
            previousY = y
            y += speed
            previousAngle = angle
            angle = (angle + angularSpeed) % MathHelper.TAU
        }
    }
}
