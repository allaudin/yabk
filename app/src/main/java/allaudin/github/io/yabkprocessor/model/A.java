package allaudin.github.io.yabkprocessor.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 6/22/17.
 *
 * @author M.Allaudin
 */

public class A<K, L, M> implements Parcelable {

    int n;

    protected A(Parcel in) {
        n = in.readInt();
    }

    public static final Creator<A> CREATOR = new Creator<A>() {
        @Override
        public A createFromParcel(Parcel in) {
            return new A(in);
        }

        @Override
        public A[] newArray(int size) {
            return new A[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(n);
    }
}
