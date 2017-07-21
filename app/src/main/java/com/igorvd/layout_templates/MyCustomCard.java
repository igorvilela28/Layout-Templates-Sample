package com.igorvd.layout_templates;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * @author Igor Vilela
 * @since 20/07/17
 */

public class MyCustomCard extends CardView {

    @Retention(SOURCE)
    @IntDef({
            TYPE_A,
            TYPE_B
    })
    public @interface CardType{}
    private transient static final int TYPE_A = 0;
    private transient static final int TYPE_B = 1;

    private Context mContext;
    private int mCardType;

    public MyCustomCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true);
        initAttrs(attrs);
        addMiddleContent();

    }

    private void initAttrs(AttributeSet attrs) {

        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyCustomCard,
                0, 0);

        try {
            mCardType = a.getInteger(R.styleable.MyCustomCard_cardType, 0);
        } finally {
            a.recycle();
        }

        //if we change some of the attributes dynamically with a method, we need to add this calls:
        //invalidate() and requestLayout() to that method;
        //more at: https://developer.android.com/training/custom-views/create-view.html#customattr

    }

    private void addMiddleContent() {

        ViewGroup root = (ViewGroup) findViewById(R.id.clCustomCardRoot);
        View placeholder = findViewById(R.id.placeholder);

        int layoutRes = retrieveMiddleLayoutRes(mCardType);

        View middleContent = LayoutInflater.from(mContext)
                .inflate(layoutRes, root, false);

        ViewGroup.LayoutParams params = placeholder.getLayoutParams();

        middleContent.setLayoutParams(params);

        root.removeView(placeholder);
        root.addView(middleContent);

    }

    @LayoutRes
    int retrieveMiddleLayoutRes(int cardType) {

        SparseIntArray layouts = new SparseIntArray();

        layouts.put(TYPE_A, R.layout.card_a);
        layouts.put(TYPE_B, R.layout.card_b);

        return layouts.get(cardType);
    }

    //https://stackoverflow.com/a/38080968
}
