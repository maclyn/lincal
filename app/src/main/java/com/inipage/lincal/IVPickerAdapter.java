package com.inipage.lincal;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class IVPickerAdapter extends RecyclerView.Adapter<IVPickerAdapter.IVPickerHolder> {
    private static final int PICKER_TYPE_COLOR = 1;
    private static final int PICKER_TYPE_ICON = 2;

    public class IVPickerHolder extends RecyclerView.ViewHolder {
        ImageView iv;
        ImageView check;
        Context context;

        public IVPickerHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.iv_picker_image);
            check = (ImageView) itemView.findViewById(R.id.iv_picker_check);
            context = iv.getContext();
        }
    }

    int mode = PICKER_TYPE_COLOR;
    String[] mResourceNames = null;
    int[] mColorList = null;
    int selectedItem = -1;

    public IVPickerAdapter(String[] resourceNames){
        this.mode = PICKER_TYPE_ICON;
        this.mResourceNames = resourceNames;
        this.selectedItem = 0;
    }

    public IVPickerAdapter(int[] colorList){
        this.mode = PICKER_TYPE_COLOR;
        this.mColorList = colorList;
        this.selectedItem = 0;
    }

    @Override
    public IVPickerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new IVPickerHolder(inflater.inflate(R.layout.item_iv_picker, parent, false));
    }

    @Override
    public void onBindViewHolder(final IVPickerHolder holder, int position) {
        if(mode == PICKER_TYPE_COLOR){
            holder.iv.setBackgroundColor(mColorList[position]);
        } else {
            holder.iv.setImageResource(holder.context.getResources().getIdentifier(mResourceNames[position], "drawable", holder.context.getPackageName()));
        }
        holder.check.setVisibility(position == selectedItem ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lastSelectedItem = selectedItem;
                selectedItem = holder.getAdapterPosition();
                holder.check.setVisibility(View.VISIBLE);
                IVPickerAdapter.this.notifyItemChanged(lastSelectedItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mode == PICKER_TYPE_COLOR ? mColorList.length : mResourceNames.length;
    }

    public int getSelectedColor(){
        if(mode == PICKER_TYPE_ICON) throw new RuntimeException("Can't get color on a resource picker!");
        return mColorList[selectedItem];
    }

    public String getSelectedResource(){
        if(mode == PICKER_TYPE_COLOR) throw new RuntimeException("Can't get resource on a color picker!");
        return mResourceNames[selectedItem];
    }
}
