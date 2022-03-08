import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

final class DecryptException extends Exception {

    /**
     * Creates the exception.
     */
    DecryptException() {
        super("Decryption failed.");
    }
}

/**
 * Reads from an encrypted {@link java.io.InputStream} and provides the decrypted data. The
 * encryption key is provided with the constructor. The initialization vector can also be provided.
 * Otherwise this vector is read from the stream.
 *
 * @author Timm Knape
 * @version $Revision: 1.5 $
 */
class CryptInputStream extends InputStream {

    /**
     * Maximum size of data that will be read from the underlying stream.
     */
    private static final int FETCH_BUFFER_SIZE = 32;

    /**
     * Underlying stream that provides the encrypted data.
     */
    private final InputStream _parent;

    /**
     * Cipher.
     */
    private final Cbc _cipher;

    private final ByteArrayOutputStream _decrypted;

    /**
     * Buffer of unencrypted data. If the buffer is completely returned, another chunk of data will
     * be decrypted.
     */
    private byte[] _buffer = null;

    /**
     * Number of {@code byte}s that are already returned from {@link CryptInputStream#_buffer}.
     */
    private int _bufferUsed = 0;

    /**
     * Buffer for storing the encrypted data.
     */
    private final byte[] _fetchBuffer = new byte[FETCH_BUFFER_SIZE];

    /**
     * Signals, if the last encrypted data was read. If we run out of buffers, the stream is at its
     * end.
     */
    private boolean _lastBufferRead = false;

    /**
     * Creates a cipher with the key and iv provided.
     *
     * @param parent Stream that provides the encrypted data
     * @param key key for the cipher algorithm
     * @param iv initial values for the CBC scheme
     */
    public CryptInputStream(InputStream parent, byte[] key, byte[] iv) {
        this._parent = parent;
        this._decrypted = new ByteArrayOutputStream();
        this._cipher = new Cbc(iv, key, this._decrypted);
    }

    /**
     * Creates a cipher with the key. The iv will be read from the {@code parent} stream. If there
     * are not enough {@code byte}s in the stream, an {@link java.io.IOException} will be raised.
     *
     * @param parent Stream that provides the encrypted data
     * @param key key for the cipher algorithm
     * @throws IOException if the iv can't be read
     */
    public CryptInputStream(InputStream parent, byte[] key) throws IOException {
        this._parent = parent;
        byte[] iv = new byte[16];
        int readed = 0;
        while (readed < 16) {
            int cur = parent.read(iv, readed, 16 - readed);
            if (cur < 0) {
                throw new IOException("No initial values in stream.");
            }
            readed += cur;
        }
        this._decrypted = new ByteArrayOutputStream();
        this._cipher = new Cbc(iv, key, this._decrypted);
    }

    /**
     * Tries to read the next decrypted data from the output stream
     */
    private void readFromStream() {
        if (this._decrypted.size() > 0) {
            this._buffer = this._decrypted.toByteArray();
            this._decrypted.reset();
        }
    }

    /**
     * Returns the next decrypted {@code byte}. If there is no more data, {@code -1} will be
     * returned. If the decryption fails or the underlying stream throws an
     * {@link java.io.IOException}, an {@link java.io.IOException} will be thrown.
     *
     * @return next decrypted {@code byte} or {@code -1}
     * @throws IOException if the decryption fails or the underlying stream throws an exception
     */
    @Override
    public int read() throws IOException {
        while (this._buffer == null || this._bufferUsed >= this._buffer.length) {
            if (this._lastBufferRead) {
                return -1;
            }

            this._bufferUsed = 0;
            this._buffer = null;

            int readed = this._parent.read(this._fetchBuffer, 0, FETCH_BUFFER_SIZE);
            if (readed < 0) {
                this._lastBufferRead = true;
                try {
                    this._cipher.finishDecryption();
                    readFromStream();
                } catch (DecryptException ex) {
                    throw new IOException("can't decrypt");
                }
            } else {
                this._cipher.decrypt(this._fetchBuffer, readed);
                readFromStream();
            }
        }

        return this._buffer[this._bufferUsed++] & 0xff;
    }

    /**
     * Closes the parent stream.
     *
     * @throws IOException if the parent stream throws an exception
     */
    @Override
    public void close() throws IOException {
        this._parent.close();
    }
}

/**
 * Encrypts the passed data and stores it into the underlying {@link java.io.OutputStream}. If no
 * initial vector is provided in the constructor, the cipher will be initialized with random data
 * and this data will be sent directly to the underlying stream.
 *
 * @author Timm Knape
 * @version $Revision: 1.5 $
 */
class CryptOutputStream extends OutputStream {

    /**
     * Cipher.
     */
    private final Cbc _cipher;

    /**
     * Buffer for sending single {@code byte}s.
     */
    private final byte[] _buffer = new byte[1];

    /**
     * Initializes the cipher with the given key and initial values.
     *
     * @param parent underlying {@link java.io.OutputStream}
     * @param key key for the cipher algorithm
     * @param iv initial values for the CBC scheme
     */
    public CryptOutputStream(OutputStream parent, byte[] key, byte[] iv) {
        this._cipher = new Cbc(iv, key, parent);
    }

    /**
     * Initializes the cipher with the given key. The initial values for the CBC scheme will be
     * random and sent to the underlying stream.
     *
     * @param parent underlying {@link java.io.OutputStream}
     * @param key key for the cipher algorithm
     * @throws IOException if the initial values can't be written to the underlying stream
     */
    public CryptOutputStream(OutputStream parent, byte[] key)
            throws IOException {
        byte[] iv = new byte[16];
        Random rnd = CryptUtils.newRandomNumberGenerator();
        rnd.nextBytes(iv);
        parent.write(iv);

        this._cipher = new Cbc(iv, key, parent);
    }

    /**
     * Encrypts a single {@code byte}.
     *
     * @param b {@code byte} to be encrypted
     * @throws IOException if encrypted data can't be written to the underlying stream
     */
    @Override
    public void write(int b) throws IOException {
        this._buffer[0] = (byte) b;
        this._cipher.encrypt(this._buffer);
    }

    /**
     * Encrypts a {@code byte} array.
     *
     * @param b {@code byte} array to be encrypted
     * @throws IOException if encrypted data can't be written to the underlying stream
     */
    @Override
    public void write(byte[] b) throws IOException {
        this._cipher.encrypt(b);
    }

    /**
     * Finalizes the encryption and closes the underlying stream.
     *
     * @throws IOException if the encryption fails or the encrypted data can't be written to the
     * underlying stream
     */
    @Override
    public void close() throws IOException {
        this._cipher.finishEncryption();
    }
}
