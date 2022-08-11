package net.catenax.dapsreg.util;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Service
public class Certutil {

    public static String getAki(X509Certificate cert) {
        byte[] extensionValue = cert.getExtensionValue("2.5.29.35");
        byte[] octets = DEROctetString.getInstance(extensionValue).getOctets();
        AuthorityKeyIdentifier authorityKeyIdentifier = AuthorityKeyIdentifier.getInstance(octets);
        byte[] keyIdentifier = authorityKeyIdentifier.getKeyIdentifier();
        return BaseEncoding.base16().upperCase().withSeparator(":", 2).encode(keyIdentifier);
    }

    public static String getSki(X509Certificate cert) {
        var extensionValue = cert.getExtensionValue("2.5.29.14");
        var octets = DEROctetString.getInstance(extensionValue).getOctets();
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier.getInstance(octets);
        var keyIdentifier = subjectKeyIdentifier.getKeyIdentifier();
        return BaseEncoding.base16().upperCase().withSeparator(":", 2).encode(keyIdentifier);
    }

    public static X509Certificate loadCertificate(String pem) throws IOException, CertificateException {
        try(var ts = new ByteArrayInputStream(pem.getBytes(Charsets.UTF_8))) {
            CertificateFactory fac  = CertificateFactory.getInstance("X509");
            return  (X509Certificate) fac.generateCertificate(ts);
        }
    }

    public static String getClientId(X509Certificate certificate) {
        return getSki(certificate).concat(":keyid:").concat(getAki(certificate));
    }

    public static String getCertificate(X509Certificate certificate) throws IOException {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter jpw = new JcaPEMWriter(sw)) {
            jpw.writeObject(certificate);
        }
        return sw.toString();
    }

}
