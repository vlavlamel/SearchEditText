package com.example.searchableedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SearchEditText extends LinearLayout {

	private int rightIconId;
	private String hint;
	private ImageView rightIcon;
	private EditText searchEditText;
	private ImageView clearBtn;

	private OnSearchTextListener onSearchTextListener;

	private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

	private Emitter<String> emitter;

	private ConnectableObservable<String> observable = Observable.create(new Action1<Emitter<String>>() {
		@Override
		public void call(Emitter<String> stringEmitter) {
			emitter = stringEmitter;
		}
	}, Emitter.BackpressureMode.LATEST)
		.debounce(600, TimeUnit.MILLISECONDS)
		.distinctUntilChanged() 
		.subscribeOn(Schedulers.io())
		.observeOn(AndroidSchedulers.mainThread())
		.replay();

	public SearchEditText(@NonNull Context context) {
		super(context);
	}

	public SearchEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context)
			.inflate(R.layout.search_edit_text, this);
		TypedArray a = context.obtainStyledAttributes(attrs,
			R.styleable.SearchEditText, 0, 0);
		rightIconId = a.getResourceId(R.styleable.SearchEditText_right_icon, -1);
		hint = a.getString(R.styleable.SearchEditText_hint);
		a.recycle();
		initViews();
	}

	private void initViews() {
		rightIcon = findViewById(R.id.right_icon);
		searchEditText = findViewById(R.id.search_edit_text);
		clearBtn = findViewById(R.id.clear_btn);
		clearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEditText.setText("");
			}
		});
		setRightIcon(rightIconId);
		setHint(hint);
		observable.connect();
		initSearchTextListener();
	}


	private void initSearchTextListener() {
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(final Editable s) {
				if (TextUtils.isEmpty(s.toString())) {
					clearBtn.setVisibility(GONE);
				} else {
					clearBtn.setVisibility(VISIBLE);
				}
				if (!mCompositeSubscription.hasSubscriptions()) {
					mCompositeSubscription.add(observable
						.subscribe(new Action1<String>() {
							@Override
							public void call(String s) {
								if (onSearchTextListener != null)
									onSearchTextListener.onSearchTextChange(s);
							}
						}));
				}
				if(emitter !=null)
					emitter.onNext(s.toString());
			}
		});
		searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					mCompositeSubscription.clear();
					if (onSearchTextListener != null)
						onSearchTextListener.onSearchTextSubmit(searchEditText.getText()
							.toString());
					return true;
				}
				return false;
			}
		});
	}

	public void setHint(String hint) {
		if (hint != null)
			searchEditText.setHint(hint);
	}

	public void setRightIcon(int id) {
		if (id != -1)
			rightIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), id));
	}

	public void setOnSearchTextListener(OnSearchTextListener onSearchTextListener) {
		this.onSearchTextListener = onSearchTextListener;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mCompositeSubscription.clear();
	}

	public interface OnSearchTextListener {

		void onSearchTextSubmit(String s);

		void onSearchTextChange(String s);
	}

}