package edu.virginia.cs4720.besafe;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ContactsAdapter extends ArrayAdapter<Contact> {

	boolean hasPrimary = false;
	ImageButton curPrimary;
	String curPrimaryNumber = "911";
	String curPrimaryName = "Police";
	boolean clicked = false;

	public ContactsAdapter(Context context, ArrayList<Contact> contacts) {
		super(context, 0, contacts);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item
		Contact contact = getItem(position);
		// Check if an existing view is being reused, otherwise inflate the view
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.adapter_contact_item, parent, false);
		}
		// Populate the data into the template view using the data object
		final TextView tvName = (TextView) view.findViewById(R.id.tvName);
		TextView tvEmail = (TextView) view.findViewById(R.id.tvEmail);
		final TextView tvPhone = (TextView) view.findViewById(R.id.tvPhone);

		final ImageButton IB = (ImageButton) view.findViewById(R.id.primaryBtn);
		IB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clicked = true;
				if (hasPrimary && curPrimary.equals(IB)){
					IB.setImageDrawable(ContextCompat.getDrawable(getContext(),android.R.drawable.btn_star_big_off));
					hasPrimary = false;
					curPrimaryName = "Police";
					curPrimaryNumber = "911";
				}else if (hasPrimary && !curPrimary.equals(IB)){
					curPrimary.setImageDrawable(ContextCompat.getDrawable(getContext(),android.R.drawable.btn_star_big_off));
					IB.setImageDrawable(ContextCompat.getDrawable(getContext(),android.R.drawable.btn_star_big_on));
					curPrimary = IB;
					curPrimaryName = tvName.getText().toString();
					curPrimaryNumber = tvPhone.getText().toString();
				}
				else{
					IB.setImageDrawable(ContextCompat.getDrawable(getContext(),android.R.drawable.btn_star_big_on));
					curPrimary = IB;
					hasPrimary = true;
					curPrimaryName = tvName.getText().toString();
					curPrimaryNumber = tvPhone.getText().toString();

				}
				//hasPrimary = !hasPrimary;
			}
		});

		tvName.setText(contact.name);
		tvEmail.setText("");
		tvPhone.setText("");
		if (contact.emails.size() > 0 && contact.emails.get(0) != null) {
			tvEmail.setText(contact.emails.get(0).address);
		}
		if (contact.numbers.size() > 0 && contact.numbers.get(0) != null) {
			tvPhone.setText(contact.numbers.get(0).number);
		}
		return view;
	}

	public String getCurPrimaryName() {
		return curPrimaryName;
	}

	public String getCurPrimaryNumber() {
		return curPrimaryNumber;
	}

	public boolean getClicked() {
		return clicked;
	}

}
