/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.EmojiTextViewLinks;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.net.URLConnection;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import static android.webkit.MimeTypeMap.getFileExtensionFromUrl;
import static com.zoffcc.applications.trifa.HelperGeneric.dp2px;
import static com.zoffcc.applications.trifa.HelperGeneric.get_vfs_image_filename_own_avatar;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.PREF__compact_chatlist;
import static com.zoffcc.applications.trifa.MainActivity.PREF__global_font_size;
import static com.zoffcc.applications.trifa.MainActivity.VFS_ENCRYPT;
import static com.zoffcc.applications.trifa.MainActivity.selected_messages;
import static com.zoffcc.applications.trifa.MessageListActivity.onClick_message_helper;
import static com.zoffcc.applications.trifa.MessageListActivity.onLongClick_message_helper;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_TEXT_SIZE;

/*
 *
 * HINT: this is used when an outgoing FT is canceled or has finished
 *
 */
public class MessageListHolder_file_outgoing_state_cancel extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private static final String TAG = "trifa.MessageListHolder";

    private Message message_;
    private Context context;

    ImageButton button_ok;
    ImageButton button_cancel;
    com.daimajia.numberprogressbar.NumberProgressBar ft_progressbar;
    ViewGroup ft_preview_container;
    ViewGroup ft_buttons_container;
    ImageButton ft_preview_image;
    EmojiTextViewLinks textView;
    ImageView imageView;
    de.hdodenhof.circleimageview.CircleImageView img_avatar;
    TextView date_time;
    ViewGroup layout_message_container;
    boolean is_selected = false;
    TextView message_text_date_string;
    ViewGroup message_text_date;

    public MessageListHolder_file_outgoing_state_cancel(View itemView, Context c)
    {
        super(itemView);

        // Log.i(TAG, "MessageListHolder");

        this.context = c;

        button_ok = (ImageButton) itemView.findViewById(R.id.ft_button_ok);
        button_cancel = (ImageButton) itemView.findViewById(R.id.ft_button_cancel);
        ft_progressbar = (com.daimajia.numberprogressbar.NumberProgressBar) itemView.findViewById(R.id.ft_progressbar);
        ft_preview_container = (ViewGroup) itemView.findViewById(R.id.ft_preview_container);
        ft_buttons_container = (ViewGroup) itemView.findViewById(R.id.ft_buttons_container);
        ft_preview_image = (ImageButton) itemView.findViewById(R.id.ft_preview_image);
        textView = (EmojiTextViewLinks) itemView.findViewById(R.id.m_text);
        imageView = (ImageView) itemView.findViewById(R.id.m_icon);
        img_avatar = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_avatar);
        date_time = (TextView) itemView.findViewById(R.id.date_time);
        layout_message_container = (ViewGroup) itemView.findViewById(R.id.layout_message_container);
        message_text_date_string = (TextView) itemView.findViewById(R.id.message_text_date_string);
        message_text_date = (ViewGroup) itemView.findViewById(R.id.message_text_date);
    }

    public void bindMessageList(Message m)
    {
        // Log.i(TAG, "bindMessageList");

        if (m == null)
        {
            // TODO: should never be null!!
            // only afer a crash
            m = new Message();
        }

        message_ = m;

        is_selected = false;
        if (selected_messages.isEmpty())
        {
            is_selected = false;
        }
        else
        {
            is_selected = selected_messages.contains(m.id);
        }

        if (is_selected)
        {
            layout_message_container.setBackgroundColor(Color.GRAY);
        }
        else
        {
            layout_message_container.setBackgroundColor(Color.TRANSPARENT);
        }

        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        message_text_date.setVisibility(View.GONE);
        int my_position = this.getAdapterPosition();
        if (my_position != RecyclerView.NO_POSITION)
        {
            if (MainActivity.message_list_fragment != null)
            {
                if (MainActivity.message_list_fragment.adapter != null)
                {
                    if (my_position < 1)
                    {
                        message_text_date_string.setText(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                        message_text_date.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if (!MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position).equals(
                                MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position - 1)))
                        {
                            message_text_date_string.setText(
                                    MainActivity.message_list_fragment.adapter.getDateHeaderText(my_position));
                            message_text_date.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------
        // --------- message date header (show only if different from previous message) ---------


        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        layout_message_container.setOnClickListener(onclick_listener);
        layout_message_container.setOnLongClickListener(onlongclick_listener);

        date_time.setText(long_date_time_format(m.sent_timestamp));

        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);

        final Message message = m;

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);

        if (message.filedb_id == -1) // tranfser was canceled somewhere
        {

            textView.addAutoLinkMode(AutoLinkMode.MODE_URL, AutoLinkMode.MODE_EMAIL, AutoLinkMode.MODE_HASHTAG,
                                     AutoLinkMode.MODE_MENTION);
            textView.setAutoLinkText("" + message.text + "\n *canceled*");

            ft_preview_image.setImageDrawable(null);
            ft_preview_image.setVisibility(View.GONE);
            ft_progressbar.setVisibility(View.GONE);
            ft_buttons_container.setVisibility(View.GONE);
            ft_preview_container.setVisibility(View.GONE);
            button_ok.setVisibility(View.GONE);
            button_cancel.setVisibility(View.GONE);
        }
        else // file transferred OK
        {
            // TODO: show preview and "click" to open/delete file
            textView.setAutoLinkText("" + message.text + "\n OK");

            boolean is_image = false;
            try
            {
                String mimeType = null;
                if (message.storage_frame_work)
                {
                    Uri uri = Uri.parse(message.filename_fullpath);
                    DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
                    String fileName = documentFile.getName();
                    mimeType = URLConnection.guessContentTypeFromName(fileName.toLowerCase());
                }
                else
                {
                    mimeType = URLConnection.guessContentTypeFromName(message.filename_fullpath.toLowerCase());
                }

                if (mimeType.startsWith("image/"))
                {
                    is_image = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (is_image)
            {
                ft_preview_image.setImageResource(R.drawable.round_loading_animation);

                if (PREF__compact_chatlist)
                {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.GONE);
                }
                else
                {
                    textView.setVisibility(View.VISIBLE);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE[PREF__global_font_size]);
                }

                ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        if (event.getAction() == MotionEvent.ACTION_UP)
                        {
                            try
                            {
                                if (message.storage_frame_work)
                                {
                                    Uri uri = Uri.parse(message.filename_fullpath);
                                    DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
                                    String fileName = documentFile.getName();

                                    Intent intent = new Intent(v.getContext(), ImageviewerActivity_SD.class);
                                    intent.putExtra("image_filename", uri.toString());
                                    intent.putExtra("storage_frame_work", "1");
                                    v.getContext().startActivity(intent);
                                }
                                else
                                {
                                    Intent intent = new Intent(v.getContext(), ImageviewerActivity_SD.class);
                                    intent.putExtra("image_filename", message.filename_fullpath);
                                    v.getContext().startActivity(intent);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                Log.i(TAG, "open_attachment_intent:EE:" + e.getMessage());
                            }
                        }
                        else
                        {
                        }
                        return true;
                    }
                });

                if (message.storage_frame_work)
                {
                    try
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(
                                new RoundedCorners((int) dp2px(20)));

                        GlideApp.
                                with(context).
                                load(Uri.parse(message.filename_fullpath)).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                placeholder(R.drawable.round_loading_animation).
                                into(ft_preview_image);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    java.io.File f2 = new java.io.File(message.filename_fullpath);
                    try
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter().optionalTransform(
                                new RoundedCorners((int) dp2px(20)));

                        GlideApp.
                                with(context).
                                load(f2).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                priority(Priority.LOW).
                                placeholder(R.drawable.round_loading_animation).
                                into(ft_preview_image);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                final Drawable d3 = new IconicsDrawable(this.context).
                        icon(GoogleMaterial.Icon.gmd_attachment).
                        backgroundColor(Color.TRANSPARENT).
                        color(Color.parseColor("#AA000000")).sizeDp(50);

                // ft_preview_image.setImageDrawable(d3);
                GlideApp.
                        with(context).
                        load(d3).
                        diskCacheStrategy(DiskCacheStrategy.NONE).
                        skipMemoryCache(false).
                        priority(Priority.LOW).
                        placeholder(R.drawable.round_loading_animation).
                        into(ft_preview_image);

                // open non-image files -----------------
                ft_preview_image.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        if (event.getAction() == MotionEvent.ACTION_UP)
                        {
                            if (message.storage_frame_work)
                            {
                                try
                                {
                                    Uri file_uri = Uri.parse(message.filename_fullpath);
                                    Intent newIntent = new Intent(Intent.ACTION_VIEW, file_uri);
                                    if (Build.VERSION.SDK_INT > 23)
                                    {
                                        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    }

                                    try
                                    {
                                        context.startActivity(newIntent);
                                    }
                                    catch (ActivityNotFoundException e)
                                    {
                                        Toast.makeText(context, "Can not handle this file", Toast.LENGTH_LONG).show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                try
                                {
                                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                                    Intent newIntent = new Intent(Intent.ACTION_VIEW);
                                    String mimeType = myMime.getMimeTypeFromExtension(
                                            getFileExtensionFromUrl(message.filename_fullpath));

                                    Uri file_uri = null;
                                    if (Build.VERSION.SDK_INT > 23)
                                    {
                                        file_uri = FileProvider.getUriForFile(context,
                                                                              BuildConfig.APPLICATION_ID + ".provider",
                                                                              new java.io.File(
                                                                                      message.filename_fullpath));
                                        newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    }
                                    else
                                    {
                                        file_uri = Uri.fromFile(new java.io.File(message.filename_fullpath));
                                    }

                                    newIntent.setDataAndType(file_uri, mimeType);
                                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    try
                                    {
                                        context.startActivity(newIntent);
                                    }
                                    catch (ActivityNotFoundException e)
                                    {
                                        Toast.makeText(context, "Can not handle this file", Toast.LENGTH_LONG).show();
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return true;
                    }
                });
            }

            ft_preview_container.setVisibility(View.VISIBLE);
            ft_preview_image.setVisibility(View.VISIBLE);
            ft_progressbar.setVisibility(View.GONE);
            ft_buttons_container.setVisibility(View.GONE);
        }


        final Drawable d_lock = new IconicsDrawable(context).icon(FontAwesome.Icon.faw_lock).color(
                context.getResources().getColor(R.color.colorPrimaryDark)).sizeDp(50);
        img_avatar.setImageDrawable(d_lock);

        try
        {
            if (VFS_ENCRYPT)
            {
                String fname = get_vfs_image_filename_own_avatar();

                info.guardianproject.iocipher.File f1 = null;
                try
                {
                    f1 = new info.guardianproject.iocipher.File(fname);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if ((f1 != null) && (fname != null))
                {
                    if (f1.length() > 0)
                    {
                        final RequestOptions glide_options = new RequestOptions().fitCenter();
                        GlideApp.
                                with(context).
                                load(f1).
                                diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                                skipMemoryCache(false).
                                apply(glide_options).
                                into(img_avatar);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v)
    {
        // Log.i(TAG, "onClick");
    }

    @Override
    public boolean onLongClick(final View v)
    {
        // Log.i(TAG, "onLongClick");
        return true;
    }


    private View.OnClickListener onclick_listener = new View.OnClickListener()
    {
        @Override
        public void onClick(final View v)
        {
            is_selected = onClick_message_helper(v, is_selected, message_);
        }
    };

    private View.OnLongClickListener onlongclick_listener = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(final View v)
        {
            MessageListActivity.long_click_message_return res = onLongClick_message_helper(context, v, is_selected,
                                                                                           message_);
            is_selected = res.is_selected;
            return res.ret_value;
        }
    };

}
