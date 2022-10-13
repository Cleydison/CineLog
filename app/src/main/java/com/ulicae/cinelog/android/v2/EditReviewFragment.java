package com.ulicae.cinelog.android.v2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.ulicae.cinelog.KinoApplication;
import com.ulicae.cinelog.R;
import com.ulicae.cinelog.android.activities.EditReview;
import com.ulicae.cinelog.android.activities.fragments.TagChooserDialog;
import com.ulicae.cinelog.data.ServiceFactory;
import com.ulicae.cinelog.data.dto.KinoDto;
import com.ulicae.cinelog.data.services.reviews.DataService;
import com.ulicae.cinelog.data.services.tags.TagService;
import com.ulicae.cinelog.databinding.ActivityEditReviewBinding;
import com.ulicae.cinelog.databinding.ContentEditReviewBinding;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditReviewFragment extends Fragment {

    private ContentEditReviewBinding binding;
    private ActivityEditReviewBinding activityBinding;

    KinoDto kino;

    private DataService dtoService;
    private TagService tagService;

    private WishlistItemDeleter wishlistItemDeleter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        activityBinding = ActivityEditReviewBinding.inflate(getLayoutInflater());
        binding = activityBinding.editReviewContent;
        return activityBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        wishlistItemDeleter = new WishlistItemDeleter(requireContext());

        String dtoType = requireActivity().getIntent().getStringExtra("dtoType");
        dtoService = new ServiceFactory(requireContext()).create(dtoType, ((KinoApplication) requireActivity().getApplicationContext()).getDaoSession());

        tagService = new TagService(((KinoApplication) requireActivity().getApplication()).getDaoSession());

        kino = Parcels.unwrap(requireActivity().getIntent().getParcelableExtra("kino"));
        if (requireActivity().getIntent().getBooleanExtra("creation", false)) {
            requireActivity().setTitle(getString(R.string.title_activity_add_review_creation));
        }

        initRating();
        initReview();
        initKinoTitle();
        initToolbar();

        binding.reviewTagEdit.setOnClickListener(onReviewTagEdit());
        activityBinding.fabSave.setOnClickListener(fabView -> onFabSaveClick());
        binding.kinoReviewDateButton.setOnClickListener(this::showTimePickerDialog);
    }

    private void initReview() {
        if (kino.getReview() != null) {
            binding.kinoReviewText.setText(kino.getReview());
        }

        if (kino.getReview_date() != null) {
            String review_date_as_string = null;
            if (kino.getReview_date() != null) {
                review_date_as_string = DateFormat.getDateFormat(requireContext()).format(kino.getReview_date());
            }
            binding.kinoReviewDateButton.setText(review_date_as_string);
        }
    }

    @NonNull
    private View.OnClickListener onReviewTagEdit() {
        return view -> {
            TagChooserDialog dialog = new TagChooserDialog(tagService, kino);
            dialog.show(requireActivity().getSupportFragmentManager(), "NoticeDialogFragment");
        };
    }

    private void initToolbar() {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(activityBinding.editReviewToolbar.toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initRating() {
        int maxRating = getMaxRating();
        String[] displayedValues = getDisplayedValues(maxRating);

        initRatingPicker(maxRating, displayedValues);
        initDisplayedRating(displayedValues);
    }

    private int getMaxRating() {
        int maxRating;
        if (kino.getMaxRating() == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String defaultMaxRateValue = prefs.getString("default_max_rate_value", "5");
            maxRating = Integer.parseInt(defaultMaxRateValue);
        } else {
            maxRating = kino.getMaxRating();
        }
        return maxRating;
    }

    private void initRatingPicker(int maxRating, String[] displayedValues) {
        binding.ratingPicker.setMinValue(0);
        binding.ratingPicker.setMaxValue(maxRating * 2);

        binding.ratingPicker.setDisplayedValues(displayedValues);

        binding.ratingPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            String[] displayedValues1 = picker.getDisplayedValues();
            float rating = Float.parseFloat(displayedValues1[newVal]);

            binding.reviewEditRatingBarAsText.setText(String.format("%s", rating));
            kino.setRating(rating);
        });
    }

    private void initKinoTitle() {
        binding.viewKinoTitleEdit.setText(kino.getTitle());
        binding.viewKinoTitleReadonly.setText(kino.getTitle());
        if (kino.getTmdbKinoId() != null) {
            binding.viewKinoTitleEdit.setVisibility(View.INVISIBLE);
            binding.viewKinoTitleReadonly.setVisibility(View.VISIBLE);
        } else {
            binding.viewKinoTitleEdit.setVisibility(View.VISIBLE);
            binding.viewKinoTitleReadonly.setVisibility(View.INVISIBLE);
        }
    }

    private void initDisplayedRating(String[] displayedValues) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        int maxRating;
        if (kino.getMaxRating() == null) {
            String defaultMaxRateValue = prefs.getString("default_max_rate_value", "5");
            maxRating = Integer.parseInt(defaultMaxRateValue);
        } else {
            maxRating = kino.getMaxRating();
        }

        if (kino.getRating() != null) {
            binding.ratingPicker.setValue(getValueToDisplay(displayedValues, kino.getRating()));
            binding.reviewEditRatingBarAsText.setText(String.format("%s", kino.getRating()));
        }

        binding.reviewEditRatingBarMaxAsText.setText(String.format("/%s", maxRating));
    }

    private int getValueToDisplay(String[] displayedValues, float rating) {
        int i = 0;
        for (String value : displayedValues) {
            if (Float.parseFloat(value) == rating) {
                return i;
            }

            i++;
        }

        return 0;
    }

    @NonNull
    private String[] getDisplayedValues(int maxRating) {
        List<String> displayedValues = new ArrayList<>();

        for (int i = 0; i <= maxRating; i++) {
            displayedValues.add(String.valueOf(i));

            if (i != maxRating) {
                displayedValues.add(i + ".5");
            }
        }

        return displayedValues.toArray(new String[0]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showTimePickerDialog(View view) {
        new DatePickerFragment().show(getChildFragmentManager(), "timePicker");
    }

    public void onFabSaveClick() {
        kino.setReview(binding.kinoReviewText.getText().toString());

        if (kino.getTmdbKinoId() == null) {
            kino.setTitle(binding.viewKinoTitleEdit.getText().toString());
        }

        if (kino.getMaxRating() == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String maxRating = prefs.getString("default_max_rate_value", "5");
            int maxRatingAsInt = Integer.parseInt(maxRating);
            kino.setMaxRating(maxRatingAsInt);
        }

        //noinspection unchecked
        kino = (KinoDto) dtoService.createOrUpdate(kino);

        long wishlistId = requireActivity().getIntent().getLongExtra("wishlistId", 0L);
        if (wishlistId != 0L) {
            wishlistItemDeleter.deleteWishlistItem(wishlistId, requireActivity().getIntent().getStringExtra("dtoType"));
        }

        ((EditReview) requireActivity()).redirect(kino);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            KinoDto kino = ((EditReviewFragment) requireParentFragment()).kino;
            kino.setReview_date(c.getTime());
            String review_date_as_string = null;
            if (kino.getReview_date() != null) {
                review_date_as_string = DateFormat.getDateFormat(requireContext()).format(kino.getReview_date());
            }
            ((EditReviewFragment) requireParentFragment()).binding.kinoReviewDateButton.setText(review_date_as_string);
        }
    }
}
