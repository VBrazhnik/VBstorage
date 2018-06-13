package com.vbrazhnik.vbstorage.tag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public final class EditTagsActivityIntentBuilder {

    public EditTagsActivityIntentBuilder() {
    }

    public Intent build(Context context) {
        Intent intent = new Intent(context, EditTagsActivity.class);
        return intent;
    }

    public static void inject(Intent intent, EditTagsActivity activity) {
        Bundle extras = intent.getExtras();
    }
}