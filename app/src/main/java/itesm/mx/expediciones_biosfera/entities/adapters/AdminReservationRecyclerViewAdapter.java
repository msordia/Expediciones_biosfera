package itesm.mx.expediciones_biosfera.entities.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import itesm.mx.expediciones_biosfera.R;
import itesm.mx.expediciones_biosfera.behavior.activities.ReservationAdminDetailActivity;
import itesm.mx.expediciones_biosfera.entities.models.Customer;
import itesm.mx.expediciones_biosfera.entities.models.Destination;
import itesm.mx.expediciones_biosfera.entities.models.Reservation;
import itesm.mx.expediciones_biosfera.utilities.StringFormatHelper;

public class AdminReservationRecyclerViewAdapter extends RecyclerView.Adapter<AdminReservationRecyclerViewAdapter.ViewHolder> {
    private List<Reservation> reservationList;
    private Context context;
    private FirebaseFirestore firestoreDB;

    public AdminReservationRecyclerViewAdapter(List<Reservation> reservationList, Context context, FirebaseFirestore firestoreDB){
        this.reservationList = reservationList;
        this.context = context;
        this.firestoreDB = firestoreDB;
    }

    @Override
    public AdminReservationRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_reservation, parent, false);
        return new AdminReservationRecyclerViewAdapter.ViewHolder(view);
    }

    private String getStatusMessage(String status) {
        Resources resources = this.context.getResources();
        int resourceId;
        switch (status) {
            case "approved":
                resourceId = R.string.approved;
                break;
            case "declined":
                resourceId = R.string.denied;
                break;
            case "pending":
                resourceId = R.string.pending;
                break;
            default:
                resourceId = R.string.pending;
        }
        return resources.getString(resourceId);
    }

    @Override
    public void onBindViewHolder(final AdminReservationRecyclerViewAdapter.ViewHolder holder, int position){
        final int itemPosition = position;
        final Reservation reservation = reservationList.get(itemPosition);
        firestoreDB.collection("destinations").document(reservation.getTripReference())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                if(snapshot.exists()) {
                    Destination destination = snapshot.toObject(Destination.class);
                    holder.tvDestination.setText(destination.getName());
                }
                else{
                    holder.tvDestination.setText("Destination not found");
                }
            }
        });
        firestoreDB.collection("users").document(reservation.getCustomerReference())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot snapshot = task.getResult();
                if(snapshot.exists()) {
                    Customer customer = snapshot.toObject(Customer.class);
                    holder.tvCustomer.setText(customer.getName());
                    holder.tvEmail.setText(customer.getEmail());
                    holder.tvEmail.setVisibility(View.GONE);
                }
                else{
                    holder.tvCustomer.setText("User not found");
                }
            }
        });

        holder.tvPrice.setText(StringFormatHelper.getPriceFormat(reservation.getPrice(), context.getResources()));
        holder.tvDate.setText(StringFormatHelper.getDateAsString(reservation.getInitialDate(), true));
        if(reservation.getIsPaid() != null) {
            String status = "";
            if (!reservation.getIsConfirmed().equals("approved")) {
                status = String.format(context.getString(R.string.confirmation_text), getStatusMessage(reservation.getIsConfirmed()));
            } else if (reservation.getIsPaid() != null) {
                status = String.format(context.getString(R.string.payment_text), getStatusMessage(reservation.getIsPaid()));
            }
            holder.tvStatus.setText(status);
        }
    }

    @Override
    public int getItemCount(){
        return reservationList.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView tvDate, tvPrice, tvCustomer, tvEmail, tvDestination, tvStatus;
        ViewHolder(View view){
            super(view);
            tvDate = view.findViewById(R.id.text_date);
            tvCustomer = view.findViewById(R.id.text_customer);
            tvEmail = view.findViewById(R.id.text_customer_email);
            tvDestination = view.findViewById(R.id.text_destination);
            tvPrice = view.findViewById(R.id.text_price);
            tvStatus = view.findViewById(R.id.text_status);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int position = getAdapterPosition();
                    Intent intent = new	Intent(context, ReservationAdminDetailActivity.class);
                    Reservation reservation = reservationList.get(position);
                    intent.putExtra(ReservationAdminDetailActivity.DESTINATION, tvDestination.getText().toString());
                    intent.putExtra(ReservationAdminDetailActivity.CUSTOMER, tvCustomer.getText().toString());
                    intent.putExtra(ReservationAdminDetailActivity.EMAIL, tvEmail.getText().toString());
                    intent.putExtra(ReservationAdminDetailActivity.RESERVATION, reservation);
                    intent.putExtra(ReservationAdminDetailActivity.RESERVATION_REFERENCE,
                            reservation.getReference());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent);
                }
            });

        }
    }
}
