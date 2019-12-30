package saker.maven.support.impl.download;

public class ArtifactDownloadFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ArtifactDownloadFailedException() {
		super();
	}

	protected ArtifactDownloadFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ArtifactDownloadFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArtifactDownloadFailedException(String message) {
		super(message);
	}

	public ArtifactDownloadFailedException(Throwable cause) {
		super(cause);
	}

}
