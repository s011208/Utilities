
package com.bj4.yhh.utilities.calculator;

import java.math.BigDecimal;

import com.bj4.yhh.utilities.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Calculator extends FrameLayout {
    private Context mContext;

    private TextView mDisplayView, mHistoryDisplayView;

    private boolean mHasDot = false;

    private Button m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, mDot, mDivide, mMultiple, mMinus, mAdd,
            mMod, mBack, mResult;

    private static final int OPERATOR_DIVIDE = 0;

    private static final int OPERATOR_MULTIPLE = 1;

    private static final int OPERATOR_MINUS = 2;

    private static final int OPERATOR_ADD = 3;

    private static final int OPERATOR_MOD = 4;

    private static final int OPERATOR_REC = 5;

    private static final int OPERATOR_RESULT = 6;

    public Calculator(Context context) {
        this(context, null);
    }

    public Calculator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Calculator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public void onFinishInflate() {
        super.onFinishInflate();
    }

    private void calculate(int operator) {
        mHasDot = false;
        switch (operator) {
            case OPERATOR_DIVIDE:
                addValue(" / ");
                break;
            case OPERATOR_MULTIPLE:
                addValue(" * ");
                break;
            case OPERATOR_MINUS:
                addValue(" - ");
                break;
            case OPERATOR_ADD:
                addValue(" + ");
                break;
            case OPERATOR_MOD:
                addValue(" % ");
                break;
            case OPERATOR_REC:
                String text = mHistoryDisplayView.getText().toString();
                if (text.length() > 0) {
                    mHistoryDisplayView.setText(text.substring(0, text.length() - 1));
                    parse();
                }
                break;
            case OPERATOR_RESULT:
                mHistoryDisplayView.setText(mDisplayView.getText());
                mDisplayView.setText("");
                break;
        }
    }

    private void addValue(String newText) {
        String txt = mHistoryDisplayView.getText().toString();
        if (txt.length() > 0) {
            String lastChar = txt.substring(txt.length() - 1);
            if ("/".equals(lastChar) || "+".equals(lastChar) || "-".equals(lastChar)
                    || "*".equals(lastChar) || "%".equals(lastChar)) {
                txt = txt + " ";
            }
        }
        mHistoryDisplayView.setText(txt + newText);
        parse();
    }

    private void parse() {
        String text = mHistoryDisplayView.getText().toString();
        String[] operators = text.trim().replace("  ", " ").replace("  ", " ").split(" ");
        boolean isSuccess = false;
        boolean getFirstValue = false;
        double currentValue = 0, comingValue = 0;
        String previousOp = "n";
        for (String operator : operators) {
            if ("".equals(operator) || " ".equals(operator))
                continue;
            if ("/".equals(operator)) {
                previousOp = operator;
            } else if ("*".equals(operator)) {
                previousOp = operator;
            } else if ("+".equals(operator)) {
                previousOp = operator;
            } else if ("-".equals(operator)) {
                previousOp = operator;
            } else if ("%".equals(operator)) {
                previousOp = operator;
            } else {
                if (getFirstValue == false) {
                    currentValue = Double.valueOf(operator);
                    getFirstValue = true;
                } else {
                    comingValue = Double.valueOf(operator);
                    if ("/".equals(previousOp)) {
                        if (comingValue == 0) {
                            currentValue = 0;
                        } else {
                            currentValue /= comingValue;
                        }
                    } else if ("*".equals(previousOp)) {
                        currentValue *= comingValue;
                    } else if ("+".equals(previousOp)) {
                        currentValue += comingValue;
                    } else if ("-".equals(previousOp)) {
                        currentValue -= comingValue;
                    } else if ("%".equals(previousOp)) {
                        currentValue %= comingValue;
                    }
                    previousOp = "n";
                }
            }
        }
        isSuccess = true;
        if (isSuccess) {
            currentValue = new BigDecimal(currentValue).setScale(4, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            mDisplayView.setText(currentValue + "");
        } else {
            mDisplayView.setText("Error");
        }
    }

    private void init() {
        View mContentView = ((LayoutInflater)mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.calculator,
                null);
        mHistoryDisplayView = (TextView)mContentView
                .findViewById(R.id.calculator_history_display_view);
        mDisplayView = (TextView)mContentView.findViewById(R.id.calculator_display_view);
        m0 = (Button)mContentView.findViewById(R.id.calculator_0);
        m1 = (Button)mContentView.findViewById(R.id.calculator_1);
        m2 = (Button)mContentView.findViewById(R.id.calculator_2);
        m3 = (Button)mContentView.findViewById(R.id.calculator_3);
        m4 = (Button)mContentView.findViewById(R.id.calculator_4);
        m5 = (Button)mContentView.findViewById(R.id.calculator_5);
        m6 = (Button)mContentView.findViewById(R.id.calculator_6);
        m7 = (Button)mContentView.findViewById(R.id.calculator_7);
        m8 = (Button)mContentView.findViewById(R.id.calculator_8);
        m9 = (Button)mContentView.findViewById(R.id.calculator_9);
        mDot = (Button)mContentView.findViewById(R.id.calculator_dot);
        mDivide = (Button)mContentView.findViewById(R.id.calculator_divide);
        mMultiple = (Button)mContentView.findViewById(R.id.calculator_multiple);
        mMinus = (Button)mContentView.findViewById(R.id.calculator_minus);
        mAdd = (Button)mContentView.findViewById(R.id.calculator_add);
        mMod = (Button)mContentView.findViewById(R.id.calculator_mod);
        mBack = (Button)mContentView.findViewById(R.id.calculator_back);
        mResult = (Button)mContentView.findViewById(R.id.calculator_result);
        mDivide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_DIVIDE);
            }
        });
        mMultiple.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_MULTIPLE);
            }
        });
        mMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_MINUS);
            }
        });
        mAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_ADD);
            }
        });
        mMod.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_MOD);
            }
        });
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_REC);
            }
        });
        mResult.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calculate(OPERATOR_RESULT);
            }
        });
        m0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("0");
            }
        });
        m1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("1");
            }
        });
        m2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("2");
            }
        });
        m3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("3");
            }
        });
        m4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("4");
            }
        });
        m5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("5");
            }
        });
        m6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("6");
            }
        });
        m7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("7");
            }
        });
        m8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("8");
            }
        });
        m9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addValue("9");
            }
        });
        mDot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasDot == false) {
                    addValue(".");
                    mHasDot = true;
                }
            }
        });
        addView(mContentView);
    }

}
