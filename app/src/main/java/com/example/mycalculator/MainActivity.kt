package com.example.mycalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var myInput: TextView? = null
    private var isLastNum  = false
    private var isLastDot = false
    private var isLastOperator = false
    private var isFirstDigitZero = false
    private var isNotDot = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myInput = findViewById(R.id.myInput)
    }

    private fun setBooleans(isLastNum:Boolean, isLastDot:Boolean, isLastOperator:Boolean)
    {
        this.isLastNum = isLastNum
        this.isLastDot = isLastDot
        this.isLastOperator = isLastOperator
    }

    fun onDigit(view: View)
    {
        if(isScreenHasText() || isScreenHasNegative()) return
        val myText = (view as Button).text
        if(myInput?.text?.isEmpty() == true || isLastOperator)
        {
            if(myText.toString()=="0")
            {
                isFirstDigitZero = true
            }
        }else
        {
            if(isFirstDigitZero && !isLastDot)
            {
                if(myText.toString() == "0") return
                else
                {
                    isFirstDigitZero = false
                    myInput?.text = myInput?.text?.dropLast(1)
                }
            }
        }
        myInput?.append(myText)
        setBooleans(isLastNum = true, isLastDot = false, isLastOperator = false)
    }

    fun onOperator(view: View)
    {
        if(isScreenHasText() || isScreenHasNegative()) return
        if(myInput?.text?.isEmpty() == true) return
        isNotDot = false
        if(isLastDot || isLastOperator)
        {
            if(isLastDot) isLastDot = false
            if(isLastOperator) isLastOperator = false
            myInput?.text = myInput?.text?.dropLast(1)
        }
        setBooleans(isLastNum=false, isLastDot=false, isLastOperator=true)
        myInput?.append((view as Button).text)
    }

    fun onClear(view: View)
    {
        myInput?.text = ""
        setBooleans(isLastNum = false, isLastDot = false, isLastOperator = false)
        isFirstDigitZero = false
        isNotDot = false
    }

    fun onDot(view: View)
    {
        if(isScreenHasText() || isScreenHasNegative()) return
        if(isLastNum && !isNotDot)
        {
            myInput?.append(".")
            isNotDot = true
            setBooleans(isLastNum = false, isLastDot = true, isLastOperator = false)
        }
    }

    fun onBack(view: View)
    {
        if(isScreenHasText() || isScreenHasNegative()) return
        if(myInput?.text?.isNotEmpty() == true)
        {
            val myString = myInput?.text.toString()
            val cmp = myString[myString.length-1]
            if(cmp == '.')
            {
                isNotDot = false
                setBooleans(true, false, false)
            }else if(isOperator(cmp))
            {
                isLastOperator = false
            }
        }

        myInput?.text = myInput?.text?.dropLast(1)

        isFirstDigitZero = false
        if(myInput?.text?.isEmpty() == true)
        {
            setBooleans(false, false, false)
            isNotDot = false
        }else
        {
            val myString = myInput?.text.toString()
            val cmp = myString[myString.length-1]
            if(cmp =='.')
            {
                setBooleans(false, true, false)
            }
            else if(isOperator(cmp))
            {
                setBooleans(false, false, true)
                isNotDot = false
            }
        }
    }

    fun onEqual(view: View)
    {
        if(isScreenHasText() || isScreenHasNegative()) return
        if(myInput?.text?.isNotEmpty() == true)
        {
            if(isLastDot || isLastOperator)
            {
                myInput?.text = myInput?.text?.dropLast(1)
            }
            setBooleans(isLastNum = true, isLastDot = false, isLastOperator = false)
            isFirstDigitZero = false
            val expression = myInput?.text.toString()
            if(expression.contains("/0") || expression.contains("/0.0"))
            {
                setBooleans(isLastNum = false, isLastDot = false, isLastOperator = false)
                isNotDot = false
                isFirstDigitZero = false
                myInput?.text = "Infinity"
                Toast.makeText(this, "Cannot divide by Zero", Toast.LENGTH_LONG).show()
                return
            }
            val ans = evaluateExpression(expression)
            var ansString = ans.toString()
            val divideAnsString = ansString.split(".")
            if(divideAnsString[1].length>1)
            {
                if(divideAnsString[1][0] =='0' && divideAnsString[1][divideAnsString[1].length-1]=='0')
                {
                    isNotDot = false
                    ansString = divideAnsString[0]
                }
            }else
            {
                if(divideAnsString[1]=="0")
                {
                    isNotDot = false
                    ansString = divideAnsString[0]
                }
            }
            if(ansString=="0")
            {
                isFirstDigitZero = true
            }
            if(ansString.contains(".")) isNotDot = true
            setBooleans(isLastNum=true, isLastDot = false, isLastOperator = false)
            myInput?.text = ansString
        }
    }

    private fun isScreenHasText() :Boolean
    {
        if(myInput?.text.toString().lowercase() == "infinity")
        {
            Toast.makeText(this, "Please Clear", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    private fun isScreenHasNegative() : Boolean
    {
        if((myInput?.text?.isNotEmpty() == true) && (myInput?.text!!.substring(0, 1)=="-"))
        {
            Toast.makeText(this, "Cannot work with negative numbers", Toast.LENGTH_LONG).show()
            return true
        }
        return false
    }

    private fun evaluateExpression(expression: String): Double {
        val operatorStack = Stack<Char>()
        val operandStack = Stack<Double>()

        var currentOperand = ""
        for (c in expression) {
            if (c.isDigit() || c == '.') {
                currentOperand += c
            } else if (isOperator(c)) {
                if (currentOperand.isNotEmpty()) {
                    operandStack.push(currentOperand.toDouble())
                    currentOperand = ""
                }
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), c)) {
                    applyOperator(operatorStack.pop(), operandStack)
                }
                operatorStack.push(c)
            } else if (c == '(') {
                operatorStack.push(c)
            } else if (c == ')') {
                if (currentOperand.isNotEmpty()) {
                    operandStack.push(currentOperand.toDouble())
                    currentOperand = ""
                }
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    applyOperator(operatorStack.pop(), operandStack)
                }
                operatorStack.pop()
            }
        }

        if (currentOperand.isNotEmpty()) {
            operandStack.push(currentOperand.toDouble())
        }
        while (!operatorStack.isEmpty()) {
            applyOperator(operatorStack.pop(), operandStack)
        }
        return operandStack.pop()
    }

    /*fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/'
    }*/

    private fun hasHigherPrecedence(op1: Char, op2: Char): Boolean {
        return (op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-') ||
                op1 == '/' && op2 == '*' || op1 == '/' && op2 == '/'
    }

    private fun applyOperator(operator: Char, operandStack: Stack<Double>) {
        val operand2 = operandStack.pop()
        val operand1 = operandStack.pop()
        when (operator) {
            '+' -> operandStack.push(operand1 + operand2)
            '-' -> operandStack.push(operand1 - operand2)
            '*' -> operandStack.push(operand1 * operand2)
            '/' -> operandStack.push(operand1 / operand2)
        }
    }


    private fun isOperator (a: Char) : Boolean
    {
        if(a=='+' || a=='-' || a=='*' || a=='/') return true
        return false
    }
}