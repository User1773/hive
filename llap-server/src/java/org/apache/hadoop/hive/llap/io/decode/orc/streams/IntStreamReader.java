package org.apache.hadoop.hive.llap.io.decode.orc.streams;

import java.io.IOException;

import org.apache.hadoop.hive.llap.io.api.EncodedColumnBatch;
import org.apache.hadoop.hive.ql.io.orc.CompressionCodec;
import org.apache.hadoop.hive.ql.io.orc.InStream;
import org.apache.hadoop.hive.ql.io.orc.OrcProto;
import org.apache.hadoop.hive.ql.io.orc.PositionProvider;
import org.apache.hadoop.hive.ql.io.orc.RecordReaderImpl;

/**
 * Created by pjayachandran on 2/10/15.
 */
public class IntStreamReader extends RecordReaderImpl.IntTreeReader {
  private boolean isFileCompressed;
  private OrcProto.RowIndexEntry rowIndex;

  private IntStreamReader(int columnId, InStream present,
      InStream data, boolean isFileCompressed,
      OrcProto.ColumnEncoding.Kind kind,
      OrcProto.RowIndexEntry rowIndex) throws IOException {
    super(columnId, present, data, kind);
    this.isFileCompressed = isFileCompressed;
    this.rowIndex = rowIndex;

    // position the readers based on the specified row index
    PositionProvider positionProvider = new RecordReaderImpl.PositionProviderImpl(rowIndex);
    seek(positionProvider);
  }

  public void seek(PositionProvider positionProvider) throws IOException {
    super.seek(positionProvider);
  }

  public static class StreamReaderBuilder {
    private String fileName;
    private int columnIndex;
    private EncodedColumnBatch.StreamBuffer presentStream;
    private EncodedColumnBatch.StreamBuffer dataStream;
    private CompressionCodec compressionCodec;
    private int bufferSize;
    private OrcProto.RowIndexEntry rowIndex;
    private OrcProto.ColumnEncoding.Kind columnEncodingKind;

    public StreamReaderBuilder setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public StreamReaderBuilder setColumnIndex(int columnIndex) {
      this.columnIndex = columnIndex;
      return this;
    }

    public StreamReaderBuilder setPresentStream(EncodedColumnBatch.StreamBuffer presentStream) {
      this.presentStream = presentStream;
      return this;
    }

    public StreamReaderBuilder setDataStream(EncodedColumnBatch.StreamBuffer dataStream) {
      this.dataStream = dataStream;
      return this;
    }

    public StreamReaderBuilder setCompressionCodec(CompressionCodec compressionCodec) {
      this.compressionCodec = compressionCodec;
      return this;
    }

    public StreamReaderBuilder setBufferSize(int bufferSize) {
      this.bufferSize = bufferSize;
      return this;
    }

    public StreamReaderBuilder setRowIndex(OrcProto.RowIndexEntry rowIndex) {
      this.rowIndex = rowIndex;
      return this;
    }

    public StreamReaderBuilder setColumnEncodingKind(OrcProto.ColumnEncoding.Kind kind) {
      this.columnEncodingKind = kind;
      return this;
    }

    public IntStreamReader build() throws IOException {
      InStream present = null;
      if (presentStream != null) {
        present = StreamUtils
            .createInStream(OrcProto.Stream.Kind.PRESENT.name(), fileName, null, bufferSize,
                presentStream);
      }

      InStream data = null;
      if (dataStream != null) {
        data = StreamUtils
            .createInStream(OrcProto.Stream.Kind.DATA.name(), fileName, null, bufferSize,
                dataStream);
      }

      return new IntStreamReader(columnIndex, present, data,
          compressionCodec != null, columnEncodingKind, rowIndex);
    }
  }

  public static StreamReaderBuilder builder() {
    return new StreamReaderBuilder();
  }

}
