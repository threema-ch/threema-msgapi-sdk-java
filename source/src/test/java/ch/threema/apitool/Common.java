package ch.threema.apitool;

/**
 * Created by se on 17.03.15.
 */
public abstract class Common {

	public static final String myPrivateKey = "private:94af3260fa2a19adc8e82e82be598be15bc6ad6f47c8ee303cb185ef860e16d2";
	public static final String myPrivateKeyExtract = "94af3260fa2a19adc8e82e82be598be15bc6ad6f47c8ee303cb185ef860e16d2";

	public static final String myPublicKey = "public:3851ad59c96146a05b32e41c0ccd0fd639dc8cd66bf6e1cbd3c8d67e4e8f5531";
	public static final String myPublicKeyExtract = "3851ad59c96146a05b32e41c0ccd0fd639dc8cd66bf6e1cbd3c8d67e4e8f5531";

	public static final String otherPrivateKey = "private:8318e05220acd38e97ba41a9a6318688214219916075ca060f9339a6d1f7fc29";
	public static final String otherPublicKey = "public:10ac7fd937eafb806f9a05bf9afa340a99387b0063cc9cb0d1ea5505d39cc076";

	public static final String echochoPublicKey = "public:4a6a1b34dcef15d43cb74de2fd36091be99fbbaf126d099d47d83d919712c72b";
	public static final String randomNonce = "516f4f1562dda0704a7bae8997cf0b354c6980181152ac32";

	public static boolean isEmpty(byte[] byteArray) {
		if(byteArray == null) {
			return true;
		}
		else {
			for(byte b: byteArray) {
				if(b != 0) {
					return false;
				}
			}
		}

		return true;
	}
}
