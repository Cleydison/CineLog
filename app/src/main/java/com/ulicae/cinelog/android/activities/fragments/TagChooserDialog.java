package com.ulicae.cinelog.android.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.ulicae.cinelog.data.dto.KinoDto;
import com.ulicae.cinelog.data.dto.TagDto;
import com.ulicae.cinelog.data.services.tags.TagService;

import java.util.ArrayList;
import java.util.List;

/**
 * CineLog Copyright 2022 Pierre Rognon
 *
 *
 * This file is part of CineLog.
 * CineLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CineLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CineLog. If not, see <https://www.gnu.org/licenses/>.
 *
 */
public class TagChooserDialog extends DialogFragment {

    private final TagService tagService;
    private final KinoDto kinoDto;

    boolean[] selectedTags;
    List<TagDto> allTags = new ArrayList<>();

    public TagChooserDialog(TagService tagService, KinoDto kinoDto) {
        this.tagService = tagService;
        this.kinoDto = kinoDto;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.populateTagList();

        List<String> allTagNames = new ArrayList<>();
        for(TagDto dto : allTags) {
            allTagNames.add(dto.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Tags")
                .setMultiChoiceItems(
                        allTagNames.toArray(new CharSequence[allTagNames.size()]),
                        selectedTags,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                selectedTags[which] = isChecked;
                            }
                        })
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        updateTagJoin();
                    }
                });

        return builder.create();

    }

    private void populateTagList() {
        allTags = tagService.getAll();
        selectedTags = new boolean[allTags.size()];

        for (int i = 0; i < allTags.size(); i++) {
            selectedTags[i] = kinoDto.getTags().contains(allTags.get(i));
        }
    }

    private void updateTagJoin() {
        for (int i = 0; i < selectedTags.length; i++) {
            TagDto tag = allTags.get(i);
            if(selectedTags[i]){
                tagService.addTagToItemIfNotExists(tag, kinoDto);
            } else {
                tagService.removeTagFromItemIfExists(tag, kinoDto);
            }
        }
    }
}
