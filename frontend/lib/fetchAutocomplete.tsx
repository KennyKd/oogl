export const fetchAutocomplete = async (query: string): Promise<string[]> => {
    const response = await fetch(`http://localhost:7000/autocomplete?query=${query}`);
    if (!response.ok) {
        throw new Error("Failed to fetch autocomplete results");
    }
    return response.json();
};
