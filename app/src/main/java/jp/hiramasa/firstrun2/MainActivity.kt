package jp.hiramasa.firstrun2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.off_dialog.view.*
import java.lang.Exception
import java.lang.StringBuilder

const val CENTER_OR_RIGHT = 21
const val BOTTOM_OR_RIGHT = 85

class MainActivity : Activity() {

  private var nStr: String = ""
  private val nList = mutableListOf<Int>()
  private val oList = mutableListOf<Char>()

  private var off = "0"

  lateinit var mAdView: AdView

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
    MobileAds.initialize(this, "ca-app-pub-8221838827607460~8884445270")

    mAdView = findViewById(R.id.adView)
    val adRequest = AdRequest.Builder().build()
    mAdView.loadAd(adRequest)

    btn_0.setOnClickListener {
      if (formula.text == "") return@setOnClickListener
      if (formula.text.last() !in '0'..'9') return@setOnClickListener
      if (btn_DEL.text == getString(R.string.btn_CLR)) return@setOnClickListener

      setNumberButton("0")
    }

    btn_1.setOnClickListener {
      setNumberButton("1")
    }

    btn_2.setOnClickListener {
      setNumberButton("2")
    }

    btn_3.setOnClickListener {
      setNumberButton("3")
    }

    btn_4.setOnClickListener {
      setNumberButton("4")
    }

    btn_5.setOnClickListener {
      setNumberButton("5")
    }

    btn_6.setOnClickListener {
      setNumberButton("6")
    }

    btn_7.setOnClickListener {
      setNumberButton("7")
    }

    btn_8.setOnClickListener {
      setNumberButton("8")
    }

    btn_9.setOnClickListener {
      setNumberButton("9")
    }

    btn_plus.setOnClickListener {
      setOperatorButton('+', '−', '×', '÷')
    }

    btn_minus.setOnClickListener {
      setOperatorButton('−', '+', '×', '÷')
    }

    btn_times.setOnClickListener {
      setOperatorButton('×', '+', '−', '÷')
    }

    btn_divided.setOnClickListener {
      setOperatorButton('÷', '+', '−', '×')
    }

    btn_DEL.setOnClickListener {
      if (formula.text == "") return@setOnClickListener

      if (btn_DEL.text == getString(R.string.btn_DEL)) {
        del()
      } else {
        clear()
      }

      if (!nStr.isEmpty()) calculator()
      else if (formula.text == "") clear()

      setFormulaSize()
      formula.text = toStringWithSeparator("${formula.text}")
    }

    btn_DEL.setOnLongClickListener {
      clear()
      return@setOnLongClickListener true
    }

    btn_OFF.setOnClickListener {
      setOff()
    }

    btn_equal.setOnClickListener {
      if (formula.text == "") return@setOnClickListener
      if (oList.isEmpty()) return@setOnClickListener
      if (formula.text.last() !in '0'..'9') return@setOnClickListener

      var result = calculator()
      formula.text = result
      result = result.replace(",", "")
      nStr = result
      nList.clear()
      oList.clear()
      btn_DEL.text = getString(R.string.btn_CLR)

      setFormulaSize()
    }
  }

  private fun setNumberButton(num: String) {
    if (formula.length() > 29) return
    if (nStr.length > 8) return
    if (btn_DEL.text == getString(R.string.btn_CLR)) clear()

    formula.text = toStringWithSeparator("${formula.text}", num)
    nStr += num

    setFormulaSize()
    calculator()
  }

  @SuppressLint("SetTextI18n")
  private fun setOperatorButton(mOpe: Char, sOpe2: Char, sOpe3: Char, sOpe4: Char) {
    if (formula.text == "") return
    if (formula.length() > 29) return
    if (formula.text.last() == mOpe) return

    if (formula.text.last() == sOpe2 || formula.text.last() == sOpe3
      || formula.text.last() == sOpe4
    ) del()

    if (btn_DEL.text == getString(R.string.btn_CLR)) btn_DEL.text = getString(R.string.btn_DEL)

    formula.text = "${formula.text}$mOpe"
    addList(nStr, mOpe)
    nStr = ""

    setFormulaSize()
  }

  private fun addList(str: String, ope: Char) {
    try {
      val num = str.toInt()
      nList.add(num)
      oList.add(ope)
    } catch (e: Exception) {
      println(e)
    }
  }

  private fun del() {
    val formulaStr = formula.text.toString()
    formula.text = formulaStr.subSequence(0, formulaStr.lastIndex)
    if (!nStr.isEmpty()) {
      nStr = nStr.substring(0, nStr.lastIndex)
    } else if (!oList.isEmpty()) {
      nStr = nList.last().toString()
      nList.remove(nList.last())
      oList.remove(oList.last())
    }
  }

  private fun clear() {
    formula.text = ""
    taxExcluded.text = getString(R.string.taxExcluded)
    taxIncluded.text = getString(R.string.taxIncluded)
    nStr = ""
    nList.clear()
    oList.clear()
    btn_DEL.text = getString(R.string.btn_DEL)
  }

  @SuppressLint("SetTextI18n")
  private fun calculator(): String {
    val nList = nList.toMutableList()
    val oList = oList.toMutableList()
    nList.add(nStr.toInt())

    oList
      .filter { it == '×' || it == '÷' }
      .map {
        val i = oList.indexOfFirst { it == '×' || it == '÷' }
        val result = if (it == '×') nList[i] * nList[i + 1] else nList[i] / nList[i + 1]
        nList[i] = result
        nList.removeAt(i + 1)
        oList.removeAt(i)
      }

    oList
      .filter { it == '+' || it == '−' }
      .map {
        val result = if (it == '+') nList[0] + nList[1] else nList[0] - nList[1]
        nList[0] = result
        nList.removeAt(1)
        oList.removeAt(0)
      }

    val result = (nList[0] * (1.0 - (0.01 * off.toInt()))).toFloat().toInt()
    taxExcluded.text = getString(R.string.taxExcluded) + toStringWithSeparator(result.toString())
    taxIncluded.text = getString(R.string.taxIncluded) + toStringWithSeparator((result * 1.1).toInt().toString())

    return toStringWithSeparator((result * 1.1).toInt().toString())
  }

  private fun setFormulaSize() {
    val result = "${formula.text}".replace(",", "")
    when (result.length) {
      in 15..19 -> {
        formula.textSize = 30.0F
        formula.gravity = CENTER_OR_RIGHT
      }
      in 20..30 -> formula.textSize = 20.0F
      else -> {
        formula.textSize = 40.0F
        formula.gravity = BOTTOM_OR_RIGHT
      }
    }
  }

  private fun toStringWithSeparator(str: String, append: String = ""): String {
    var result = str + append
    result = result.replace(",", "")
    var strBuilder = StringBuilder().append(result)
    var count = 0
    for ((index, value) in strBuilder.withIndex().reversed()) {
      if (value in '0'..'9') count++ else count = 0
      if (count == 3 && index != 0) {
        if (strBuilder[index - 1] in '0'..'9')
          strBuilder = strBuilder.insert(index, ",")
        count = 0
      }
    }

    return strBuilder.toString()
  }

  @SuppressLint("SetTextI18n")
  private fun setOff() {
    val inflater = this.getSystemService(
      Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    val layout = inflater.inflate(
      R.layout.off_dialog,
      findViewById(R.id.layout_root)
    )

    layout.numPicker1.minValue = 0
    layout.numPicker1.maxValue = 9

    layout.numPicker2.minValue = 0
    layout.numPicker2.maxValue = 9

    val builder = AlertDialog.Builder(this)
    builder.setTitle(getString(R.string.off_message))
    builder.setView(layout)
    builder.setPositiveButton(getString(R.string.OK)) { _, _ ->
      off = layout.numPicker1.value.toString() + layout.numPicker2.value.toString()
      btn_OFF.text = "${off.toInt()}" + getString(R.string.btn_OFF)
      if (!nStr.isEmpty()) calculator()
    }
    builder.setNegativeButton(getString(R.string.Cancel)) { _, _ ->
    }

    builder.create().show()
  }

}
