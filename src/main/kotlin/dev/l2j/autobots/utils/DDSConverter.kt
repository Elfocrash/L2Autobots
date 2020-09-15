package dev.l2j.autobots.utils

import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO


internal object DDSConverter {

    internal fun convertToDDS(stream: InputStream): ByteBuffer? {
        val bufferedimage: BufferedImage = ImageIO.read(stream) ?: return null
        return if (bufferedimage.colorModel.hasAlpha()) {
            convertToDxt3(bufferedimage)
        } else convertToDxt1NoTransparency(bufferedimage)
    }

    internal fun convertToDDS(file: File?): ByteBuffer? {
        if (file == null) {
            val s = "nullValue.FileIsNull"
            throw IllegalArgumentException(s)
        }
        if (!file.exists() || !file.canRead()) {
            val s1 = "DDSConverter.NoFileOrNoPermission"
            throw IllegalArgumentException(s1)
        }
        val bufferedimage: BufferedImage = ImageIO.read(file) ?: return null
        return if (bufferedimage.colorModel.hasAlpha()) {
            convertToDxt3(bufferedimage)
        } else convertToDxt1NoTransparency(bufferedimage)
    }

    private fun convertToDxt1NoTransparency(bufferedimage: BufferedImage?): ByteBuffer? {
        if (bufferedimage == null) {
            return null
        }
        val ai = IntArray(16)
        val i = 128 + bufferedimage.width * bufferedimage.height / 2
        val bytebuffer: ByteBuffer = ByteBuffer.allocate(i)
        bytebuffer.order(ByteOrder.LITTLE_ENDIAN)
        buildHeaderDxt1(bytebuffer, bufferedimage.width, bufferedimage.height)
        val j = bufferedimage.width / 4
        val k = bufferedimage.height / 4
        for (l in 0 until k) {
            for (i1 in 0 until j) {
                val bufferedimage1 = bufferedimage.getSubimage(i1 * 4, l * 4, 4, 4)
                bufferedimage1.getRGB(0, 0, 4, 4, ai, 0, 4)
                val acolor = getColors888(ai)
                for (j1 in ai.indices) {
                    ai[j1] = getPixel565(acolor[j1])
                    acolor[j1] = getColor565(ai[j1])
                }
                val ai1 = determineExtremeColors(acolor)
                if (ai[ai1[0]] < ai[ai1[1]]) {
                    val k1 = ai1[0]
                    ai1[0] = ai1[1]
                    ai1[1] = k1
                }
                bytebuffer.putShort(ai[ai1[0]].toShort())
                bytebuffer.putShort(ai[ai1[1]].toShort())
                val l1 = computeBitMask(acolor, ai1)
                bytebuffer.putInt(l1.toInt())
            }
        }
        return bytebuffer
    }

    private fun convertToDxt3(bufferedimage: BufferedImage?): ByteBuffer? {
        if (bufferedimage == null) {
            return null
        }
        if (!bufferedimage.colorModel.hasAlpha()) {
            return convertToDxt1NoTransparency(bufferedimage)
        }
        val ai = IntArray(16)
        val i = 128 + bufferedimage.width * bufferedimage.height
        val bytebuffer: ByteBuffer = ByteBuffer.allocate(i)
        bytebuffer.order(ByteOrder.LITTLE_ENDIAN)
        buildHeaderDxt3(bytebuffer, bufferedimage.width, bufferedimage.height)
        val j = bufferedimage.width / 4
        val k = bufferedimage.height / 4
        for (l in 0 until k) {
            for (i1 in 0 until j) {
                val bufferedimage1 = bufferedimage.getSubimage(i1 * 4, l * 4, 4, 4)
                bufferedimage1.getRGB(0, 0, 4, 4, ai, 0, 4)
                val acolor = getColors888(ai)
                var j1 = 0
                while (j1 < ai.size) {
                    bytebuffer.put((ai[j1] ushr 28 or (ai[j1 + 1] ushr 24)).toByte())
                    j1 += 2
                }
                for (k1 in ai.indices) {
                    ai[k1] = getPixel565(acolor[k1])
                    acolor[k1] = getColor565(ai[k1])
                }
                val ai1 = determineExtremeColors(acolor)
                if (ai[ai1[0]] < ai[ai1[1]]) {
                    val l1 = ai1[0]
                    ai1[0] = ai1[1]
                    ai1[1] = l1
                }
                bytebuffer.putShort(ai[ai1[0]].toShort())
                bytebuffer.putShort(ai[ai1[1]].toShort())
                val l2 = computeBitMask(acolor, ai1)
                bytebuffer.putInt(l2.toInt())
            }
        }
        return bytebuffer
    }

    private fun buildHeaderDxt1(bytebuffer: ByteBuffer, i: Int, j: Int) {
        bytebuffer.rewind()
        bytebuffer.put(68.toByte())
        bytebuffer.put(68.toByte())
        bytebuffer.put(83.toByte())
        bytebuffer.put(32.toByte())
        bytebuffer.putInt(124)
        val k = 0xa1007
        bytebuffer.putInt(k)
        bytebuffer.putInt(j)
        bytebuffer.putInt(i)
        bytebuffer.putInt(i * j / 2)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.position(bytebuffer.position() + 44)
        bytebuffer.putInt(32)
        bytebuffer.putInt(4)
        bytebuffer.put(68.toByte())
        bytebuffer.put(88.toByte())
        bytebuffer.put(84.toByte())
        bytebuffer.put(49.toByte())
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(4096)
        bytebuffer.putInt(0)
        bytebuffer.position(bytebuffer.position() + 12)
    }

    private fun buildHeaderDxt3(bytebuffer: ByteBuffer, i: Int, j: Int) {
        bytebuffer.rewind()
        bytebuffer.put(68.toByte())
        bytebuffer.put(68.toByte())
        bytebuffer.put(83.toByte())
        bytebuffer.put(32.toByte())
        bytebuffer.putInt(124)
        val k = 0xa1007
        bytebuffer.putInt(k)
        bytebuffer.putInt(j)
        bytebuffer.putInt(i)
        bytebuffer.putInt(i * j)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.position(bytebuffer.position() + 44)
        bytebuffer.putInt(32)
        bytebuffer.putInt(4)
        bytebuffer.put(68.toByte())
        bytebuffer.put(88.toByte())
        bytebuffer.put(84.toByte())
        bytebuffer.put(51.toByte())
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(0)
        bytebuffer.putInt(4096)
        bytebuffer.putInt(0)
        bytebuffer.position(bytebuffer.position() + 12)
    }

    private fun determineExtremeColors(acolor: Array<Color?>): IntArray {
        var i = -0x80000000
        val ai = IntArray(2)
        for (j in 0 until acolor.size - 1) {
            for (k in j + 1 until acolor.size) {
                val l = distance(acolor[j], acolor[k])
                if (l > i) {
                    i = l
                    ai[0] = j
                    ai[1] = k
                }
            }
        }
        return ai
    }

    private fun computeBitMask(acolor: Array<Color?>, ai: IntArray): Long {
        val acolor1 = arrayOf(
                null,
                null,
                Color(),
                Color()
        )
        acolor1[0] = acolor[ai[0]]
        acolor1[1] = acolor[ai[1]]
        if (acolor1[0] == acolor1[1]) {
            return 0L
        }
        acolor1[2]!!.r = (2 * acolor1[0]!!.r + acolor1[1]!!.r + 1) / 3
        acolor1[2]!!.g = (2 * acolor1[0]!!.g + acolor1[1]!!.g + 1) / 3
        acolor1[2]!!.b = (2 * acolor1[0]!!.b + acolor1[1]!!.b + 1) / 3
        acolor1[3]!!.r = (acolor1[0]!!.r + 2 * acolor1[1]!!.r + 1) / 3
        acolor1[3]!!.g = (acolor1[0]!!.g + 2 * acolor1[1]!!.g + 1) / 3
        acolor1[3]!!.b = (acolor1[0]!!.b + 2 * acolor1[1]!!.b + 1) / 3
        var l = 0L
        for (i in acolor.indices) {
            var j = 0x7fffffff
            var k = 0
            for (i1 in acolor1.indices) {
                val j1 = distance(acolor[i], acolor1[i1])
                if (j1 < j) {
                    j = j1
                    k = i1
                }
            }
            l = l or (k shl i * 2).toLong()
        }
        return l
    }

    private fun getPixel565(color: Color?): Int {
        val i = color!!.r shr 3
        val j = color.g shr 2
        val k = color.b shr 3
        return i shl 11 or (j shl 5) or k
    }

    private fun getColor565(i: Int): Color {
        val color = Color()
        color.r = (i and 63488) shr 11
        color.g = (i and 2016) shr 5
        color.b = (i and 31)
        return color
    }

    private fun getColors888(ai: IntArray): Array<Color?> {
        val acolor = arrayOfNulls<Color>(ai.size)
        for (i in ai.indices) {
            acolor[i] = Color()
            acolor[i]!!.r = (ai[i] and 0xff0000) shr 16
            acolor[i]!!.g = (ai[i] and 65280) shr 8
            acolor[i]!!.b = (ai[i] and 255)
        }
        return acolor
    }

    internal fun distance(color: Color?, color1: Color?): Int {
        return (color1!!.r - color!!.r) * (color1.r - color.r) + (color1.g - color.g) * (color1.g - color.g) + (color1.b - color.b) * (color1.b - color.b)
    }

    internal class Color {
        override fun equals(obj: Any?): Boolean {
            if (this === obj) {
                return true
            }
            if (obj == null || javaClass != obj.javaClass) {
                return false
            }
            val color = obj as Color
            if (b != color.b) {
                return false
            }
            return if (g != color.g) {
                false
            } else r == color.r
        }

        override fun hashCode(): Int {
            var i = r
            i = 29 * i + g
            i = 29 * i + b
            return i
        }

        var r: Int
        var g: Int
        var b: Int

        constructor() {
            b = 0
            g = b
            r = g
        }

        constructor(i: Int, j: Int, k: Int) {
            r = i
            g = j
            b = k
        }
    }
}