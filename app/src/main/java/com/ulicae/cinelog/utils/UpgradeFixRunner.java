package com.ulicae.cinelog.utils;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ulicae.cinelog.BuildConfig;
import com.ulicae.cinelog.KinoApplication;
import com.ulicae.cinelog.data.SerieService;
import com.ulicae.cinelog.data.dto.SerieDto;

import java.util.List;

public class UpgradeFixRunner {

    private Context context;
    private Application application;

    public UpgradeFixRunner(Context context, Application application) {
        this.context = context;
        this.application = application;
    }

    public void runFixesIfNeeded() {
        PreferencesWrapper preferencesWrapper = new PreferencesWrapper();
        int lastCodeVersionSaved = preferencesWrapper.getIntegerPreference(context, "last_code_version_saved", 0);

        if (lastCodeVersionSaved != BuildConfig.VERSION_CODE) {
            try {
                lookForFixes();
            } catch (Exception e) {
                Log.i("upgrade_fix", "Unable to process with fixes. Won't upgrade preference version code.");
                return;
            }
            preferencesWrapper.setIntegerPreference(context, "last_code_version_saved", BuildConfig.VERSION_CODE);
        }
    }

    private void lookForFixes() {
        if (BuildConfig.VERSION_CODE == 19) {
            fixSerieReviews();
        }
    }

    private void fixSerieReviews() {
        SerieService serieService = new SerieService(((KinoApplication) application).getDaoSession(), context);

        List<SerieDto> all = serieService.getAll();
        for (SerieDto serieDto : all) {
            if (serieDto.getReviewId() == 0L) {
                serieDto.setReviewId(null);

                serieService.createOrUpdate(serieDto);
                Log.i("upgrade_fix", String.format("Creating own review for serie with id %s", serieDto.getTmdbKinoId()));
            }

            serieService.syncWithTmdb(serieDto.getTmdbKinoId());
            Log.i("upgrade_fix", String.format("Refreshing data of %s serie with tmdb online db", serieDto.getTmdbKinoId()));
        }
    }
}
