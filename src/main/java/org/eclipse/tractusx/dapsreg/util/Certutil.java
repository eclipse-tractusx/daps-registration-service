/********************************************************************************
 * Copyright (c) 2021,2022 T-Systems International GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.dapsreg.util;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class Certutil {

    private Certutil() {
        throw new IllegalStateException("Utility class");
    }

    public static String getAki(X509Certificate cert) {
        byte[] extensionValue = cert.getExtensionValue("2.5.29.35");
        byte[] octets = ASN1OctetString.getInstance(extensionValue).getOctets();
        AuthorityKeyIdentifier authorityKeyIdentifier = AuthorityKeyIdentifier.getInstance(octets);
        byte[] keyIdentifier = authorityKeyIdentifier.getKeyIdentifier();
        return BaseEncoding.base16().upperCase().withSeparator(":", 2).encode(keyIdentifier);
    }

    public static String getSki(X509Certificate cert) {
        var extensionValue = cert.getExtensionValue("2.5.29.14");
        var octets = ASN1OctetString.getInstance(extensionValue).getOctets();
        SubjectKeyIdentifier subjectKeyIdentifier = SubjectKeyIdentifier.getInstance(octets);
        var keyIdentifier = subjectKeyIdentifier.getKeyIdentifier();
        return BaseEncoding.base16().upperCase().withSeparator(":", 2).encode(keyIdentifier);
    }

    public static String createSki(X509Certificate cert) throws NoSuchAlgorithmException {
        var publicKey = cert.getPublicKey();
        var r = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKey).getKeyIdentifier();
        return BaseEncoding.base16().upperCase().withSeparator(":", 2).encode(r);
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
