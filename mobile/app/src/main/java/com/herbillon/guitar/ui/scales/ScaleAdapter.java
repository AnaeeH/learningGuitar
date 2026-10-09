package com.herbillon.guitar.ui.scales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.herbillon.guitar.R;
import com.herbillon.guitar.model.Scale;
import com.herbillon.guitar.model.ScalePosition;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ScaleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_POSITION = 1;

    public interface OnScaleHeaderClickListener {
        void onHeaderClick(Scale scale);
    }

    private final List<Object> items;
    private final Set<Integer> collapsedScaleIds;
    private final OnScaleHeaderClickListener headerClickListener;
    private final Consumer<ScalePosition> onScaleClick;

    public ScaleAdapter(List<Object> items,
                        Set<Integer> collapsedScaleIds,
                        OnScaleHeaderClickListener headerClickListener,
                        Consumer<ScalePosition> onScaleClick) {
        this.items = items;
        this.collapsedScaleIds = collapsedScaleIds;
        this.headerClickListener = headerClickListener;
        this.onScaleClick = onScaleClick;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView chevron;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.scaleHeader);
            chevron = itemView.findViewById(R.id.scaleHeaderChevron);
        }
    }

    public static class PositionViewHolder extends RecyclerView.ViewHolder {
        ScaleDiagramView diagram;
        TextView name;
        TextView label;

        public PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            diagram = itemView.findViewById(R.id.scaleDiagram);
            name = itemView.findViewById(R.id.positionName);
            label = itemView.findViewById(R.id.positionLabel);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Scale ? VIEW_TYPE_HEADER : VIEW_TYPE_POSITION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_scale_header, parent, false));
        }
        return new PositionViewHolder(inflater.inflate(R.layout.item_scale_position, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            Scale scale = (Scale) item;

            String title = scale.getName();
            if (scale.getRootLabel() != null) {
                title += " en " + scale.getRootLabel();
            }
            h.title.setText(title);

            boolean collapsed = collapsedScaleIds.contains(scale.getId());
            h.chevron.setText(collapsed ? "▸" : "▾");

            h.itemView.setOnClickListener(v -> {
                if (headerClickListener != null) {
                    headerClickListener.onHeaderClick(scale);
                }
            });

        } else {
            PositionViewHolder h = (PositionViewHolder) holder;
            ScalePosition pos = (ScalePosition) item;
            h.name.setText("Position " + pos.getNumber());
            h.label.setText("Frette " + pos.getStartingFret());
            h.diagram.setPosition(pos);
            holder.itemView.setOnClickListener(v -> onScaleClick.accept(pos));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup(int spanCount) {
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getItemViewType(position) == VIEW_TYPE_HEADER ? spanCount : 1;
            }
        };
    }
}