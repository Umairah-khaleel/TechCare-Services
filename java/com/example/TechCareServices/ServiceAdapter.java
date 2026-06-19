package com.example.TechCareServices;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private final List<Service> list;
    private final String userRole;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Service service);
    }

    public ServiceAdapter(List<Service> list, String userRole, OnDeleteClickListener deleteListener) {
        this.list = list;
        this.userRole = userRole;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service s = list.get(position);

        holder.name.setText(s.getName());
        holder.cat.setText(s.getCategory());
        holder.fee.setText(s.getFee());

        // NEW: Binding Time and Description
        holder.time.setText("Estimated Time: " + s.getEstimateTime());
        holder.description.setText(s.getDescription());

        Glide.with(holder.itemView.getContext())
                .load(s.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.img);

        if ("Admin".equalsIgnoreCase(userRole)) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(s);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, cat, fee, time, description; // Added time and description here
        ImageView img;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvServiceName);
            cat = itemView.findViewById(R.id.tvServiceCategory);
            fee = itemView.findViewById(R.id.tvServiceFee);
            time = itemView.findViewById(R.id.tvServiceTime); // Linked new ID
            description = itemView.findViewById(R.id.tvServiceDescription); // Linked new ID
            img = itemView.findViewById(R.id.ivServiceCardImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
        }
    }
}