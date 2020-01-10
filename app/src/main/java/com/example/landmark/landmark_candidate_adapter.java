package com.example.landmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;

import java.util.List;

public class landmark_candidate_adapter extends RecyclerView.Adapter<landmark_candidate_adapter.ViewHolder>{
    private List<FirebaseVisionCloudLandmark> mData = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView landmark_name ;
        TextView landmark_confidence ;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            landmark_name = itemView.findViewById(R.id.landmark_candidate_item) ;
            landmark_confidence = itemView.findViewById(R.id.landmark_candidate_confidence) ;
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    landmark_candidate_adapter(List<FirebaseVisionCloudLandmark> list) {
        mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public landmark_candidate_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.landmark_candidate, parent, false) ;
        landmark_candidate_adapter.ViewHolder vh = new landmark_candidate_adapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(landmark_candidate_adapter.ViewHolder holder, int position) {
        String name = mData.get(position).getLandmark() ;
        float confidence = mData.get(position).getConfidence() ;
        holder.landmark_name.setText(name) ;
        holder.landmark_confidence.setText(String.format("%.2f",confidence*100)+"%") ;
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}

