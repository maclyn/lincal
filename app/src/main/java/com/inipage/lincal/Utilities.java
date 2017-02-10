package com.inipage.lincal;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.View;

/**
 * Created by Maclyn on 2/9/2017.
 * TODO: Add a JavaDoc for this class.
 */
public class Utilities {
    public interface MenuPrepListener {
        void onPrepMenu(Menu menu);
    }

    /** See http://stackoverflow.com/questions/15454995/popupmenu-with-icons for a discussion. **/
    public static void attachIconPopupMenu(final View anchor,
                                         final int menuId,
                                         final MenuPrepListener prepListener,
                                         final PopupMenu.OnMenuItemClickListener listener){
        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
                menu.inflate(menuId);
                menu.setOnMenuItemClickListener(listener);
                prepListener.onPrepMenu(menu.getMenu());
                MenuPopupHelper helper = new MenuPopupHelper(anchor.getContext(),
                        (MenuBuilder) menu.getMenu(),
                        anchor);
                helper.setForceShowIcon(true);
                helper.show();
            }
        });
    }

    public interface ConfirmationListener {
        void onConfirmed(Context ctx);
        void onRejected(Context ctx);
    }

    public static void showConfirmationDialog(final Context ctx, int titleRes, int messageRes,
                                              int yesRes, int noRes, final ConfirmationListener listener){
        new AlertDialog.Builder(ctx)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(yesRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirmed(ctx);
                    }
                })
                .setNegativeButton(noRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onRejected(ctx);
                    }
                })
                .show();
    }
}
