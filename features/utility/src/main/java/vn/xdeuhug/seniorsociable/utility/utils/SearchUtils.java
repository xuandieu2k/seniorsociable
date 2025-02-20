package vn.xdeuhug.seniorsociable.utility.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import vn.xdeuhug.seniorsociable.utility.model.Diseases;

public class SearchUtils {

    private ExecutorService executor;

    public SearchUtils() {
        int numThreads = Runtime.getRuntime().availableProcessors(); // Số lượng luồng tương đương với số lõi CPU
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public List<Diseases> searchDiseases(List<Diseases> userList, String searchQuery) {
        List<Future<List<Diseases>>> searchResults = new ArrayList<>();

        // Tạo các Callable để thực hiện tìm kiếm trên từng đối tượng Diseases
        for (Diseases diseases : userList) {
            Callable<List<Diseases>> searchCallable = () -> {
                List<Diseases> result = new ArrayList<>();
                String name = diseases.getName();
                String overview = diseases.getOverview();
                String reason = diseases.getReason();
                String symptom = diseases.getSymptom();
                String transmission_route = diseases.getTransmissionRoute();
                String subjects_at_risk = diseases.getSubjectsAtRisk();
                String prevent = diseases.getPrevent();
                String diagnostic_measures = diseases.getDiagnosticMeasures();
                String treatment_measures = diseases.getTreatmentMeasures();

                if ((name != null && removeAccent(name.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (overview != null && removeAccent(overview.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (reason != null && removeAccent(reason.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (symptom != null && removeAccent(symptom.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (transmission_route != null && removeAccent(transmission_route.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (subjects_at_risk != null && removeAccent(subjects_at_risk.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (prevent != null && removeAccent(prevent.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (diagnostic_measures != null && removeAccent(diagnostic_measures.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                        || (treatment_measures != null && removeAccent(treatment_measures.toLowerCase()).contains(removeAccent(searchQuery.toLowerCase())))
                ) {
                    result.add(diseases);
                }
                return result;
            };
            // Đẩy Callable vào ExecutorService để thực hiện tìm kiếm
            Future<List<Diseases>> searchResult = executor.submit(searchCallable);
            searchResults.add(searchResult);
        }

        // Tổng hợp kết quả từ các tìm kiếm song song
        List<Diseases> result = new ArrayList<>();
        for (Future<List<Diseases>> future : searchResults) {
            try {
                List<Diseases> searchResult = future.get();
                result.addAll(searchResult);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String removeAccent(String input) {
        String result = input.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        result = result.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        result = result.replaceAll("[ìíịỉĩ]", "i");
        result = result.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        result = result.replaceAll("[ùúụủũưừứựửữ]", "u");
        result = result.replaceAll("[ỳýỵỷỹ]", "y");
        result = result.replaceAll("đ", "d");
        return result;
    }
}
